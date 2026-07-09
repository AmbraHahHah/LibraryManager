package com.library.prog.service;

import com.library.prog.dto.request.OrderRequest;
import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.dto.response.OrderResponse;
import com.library.prog.model.MovementTypeEnum;
import com.library.prog.model.Order;
import com.library.prog.model.OrderLine;
import com.library.prog.model.OrderStatusEnum;
import com.library.prog.model.StockMovement;
import com.library.prog.repository.ClientRepository;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.OrderLineRepository;
import com.library.prog.repository.OrderRepository;
import com.library.prog.repository.StockMovementRepository;
import com.library.prog.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderLineRepository orderLineRepository;
  private final ClientRepository clientRepository;
  private final CopyRepository copyRepository;
  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;

  @Transactional(readOnly = true)
  public List<OrderResponse> findAll() {
    return orderRepository.findAll().stream().map(this::toResponseWithLines).toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse findById(UUID id) {
    return orderRepository
        .findById(id)
        .map(this::toResponseWithLines)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
  }

  @Transactional
  public OrderResponse create(OrderRequest request) {
    var client =
        clientRepository
            .findById(request.clientId())
            .orElseThrow(
                () -> new EntityNotFoundException("Client not found: " + request.clientId()));

    for (var lineReq : request.lines()) {
      var copy =
          copyRepository
              .findById(lineReq.copyId())
              .orElseThrow(
                  () -> new EntityNotFoundException("Copy not found: " + lineReq.copyId()));
      var stock =
          stockRepository
              .findByCopyId(lineReq.copyId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException("Stock not found for copy: " + lineReq.copyId()));

      if (stock.getAvailableStock() < lineReq.quantity()) {
        throw new IllegalStateException(
            "Insufficient stock for copy "
                + lineReq.copyId()
                + ": available "
                + stock.getAvailableStock()
                + ", requested "
                + lineReq.quantity());
      }
    }

    BigDecimal total =
        request.lines().stream()
            .map(
                lineReq -> {
                  var copy = copyRepository.findById(lineReq.copyId()).orElseThrow();
                  return copy.getPrice().multiply(BigDecimal.valueOf(lineReq.quantity()));
                })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    var order =
        Order.builder()
            .customer(client)
            .status(OrderStatusEnum.PENDING)
            .totalAmount(total)
            .shippingFee(BigDecimal.ZERO)
            .shippingAddress(request.shippingAddress())
            .paymentMethod(request.paymentMethod())
            .build();

    order = orderRepository.save(order);

    var savedOrder = order;
    var lines = new ArrayList<OrderLine>();

    for (var lineReq : request.lines()) {
      var copy = copyRepository.findById(lineReq.copyId()).orElseThrow();
      var stock = stockRepository.findByCopyId(lineReq.copyId()).orElseThrow();

      stock.setReservedQuantity(stock.getReservedQuantity() + lineReq.quantity());
      stockRepository.save(stock);

      var orderLine =
          OrderLine.builder()
              .order(savedOrder)
              .copy(copy)
              .quantity(lineReq.quantity())
              .unitPrice(copy.getPrice())
              .discount(BigDecimal.ZERO)
              .build();

      lines.add(orderLineRepository.save(orderLine));
    }

    savedOrder.setOrderLines(lines);
    return toResponse(savedOrder, lines);
  }

  @Transactional
  public OrderResponse confirm(UUID id) {
    var order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

    if (order.getStatus() != OrderStatusEnum.PENDING) {
      throw new IllegalStateException("Only PENDING orders can be confirmed");
    }

    for (var line : order.getOrderLines()) {
      var stock =
          stockRepository
              .findByCopyId(line.getCopy().getId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Stock not found for copy: " + line.getCopy().getId()));

      stock.setAvailableQuantity(stock.getAvailableQuantity() - line.getQuantity());
      stock.setReservedQuantity(stock.getReservedQuantity() - line.getQuantity());
      stockRepository.save(stock);

      var movement =
          StockMovement.builder()
              .quantity(line.getQuantity())
              .movementType(MovementTypeEnum.SALE)
              .copy(line.getCopy())
              .order(order)
              .build();
      stockMovementRepository.save(movement);
    }

    order.setStatus(OrderStatusEnum.CONFIRMED);
    return toResponseWithLines(orderRepository.save(order));
  }

  @Transactional
  public void cancel(UUID id) {
    var order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

    if (order.getStatus() == OrderStatusEnum.CANCELLED
        || order.getStatus() == OrderStatusEnum.REFUNDED) {
      throw new IllegalStateException("Order is already cancelled or refunded");
    }

    for (var line : order.getOrderLines()) {
      var stock =
          stockRepository
              .findByCopyId(line.getCopy().getId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Stock not found for copy: " + line.getCopy().getId()));

      if (order.getStatus() == OrderStatusEnum.PENDING) {
        stock.setReservedQuantity(stock.getReservedQuantity() - line.getQuantity());
      } else {
        stock.setAvailableQuantity(stock.getAvailableQuantity() + line.getQuantity());
      }
      stockRepository.save(stock);
    }

    order.setStatus(OrderStatusEnum.CANCELLED);
    orderRepository.save(order);
  }

  @Transactional
  public void delete(UUID id) {
    var order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

    if (order.getStatus() != OrderStatusEnum.PENDING) {
      throw new IllegalStateException("Only PENDING orders can be deleted");
    }

    for (var line : order.getOrderLines()) {
      var stock =
          stockRepository
              .findByCopyId(line.getCopy().getId())
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Stock not found for copy: " + line.getCopy().getId()));

      stock.setReservedQuantity(stock.getReservedQuantity() - line.getQuantity());
      stockRepository.save(stock);
    }

    orderRepository.delete(order);
  }

  private OrderResponse toResponseWithLines(Order order) {
    var lines =
        order.getOrderLines().stream()
            .map(this::toLineResponse)
            .toList();
    return toResponse(order, order.getOrderLines());
  }

  private OrderResponse toResponse(Order order, List<OrderLine> lines) {
    return OrderResponse.builder()
        .id(order.getId())
        .clientId(order.getCustomer().getId())
        .clientName(order.getCustomer().getLastName())
        .status(order.getStatus())
        .totalAmount(order.getTotalAmount())
        .shippingFee(order.getShippingFee())
        .shippingAddress(order.getShippingAddress())
        .paymentMethod(order.getPaymentMethod())
        .paymentReference(order.getPaymentReference())
        .orderDate(order.getOrderDate())
        .lines(lines.stream().map(this::toLineResponse).toList())
        .build();
  }

  private OrderLineResponse toLineResponse(OrderLine line) {
    return OrderLineResponse.builder()
        .id(line.getId())
        .copyId(line.getCopy().getId())
        .isbn(line.getCopy().getIsbn())
        .format(line.getCopy().getFormat())
        .quantity(line.getQuantity())
        .unitPrice(line.getUnitPrice())
        .discount(line.getDiscount())
        .build();
  }
}
