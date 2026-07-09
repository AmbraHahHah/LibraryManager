package com.library.prog.service;

import com.library.prog.dto.request.OrderLineRequest;
import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.model.OrderLine;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.OrderLineRepository;
import com.library.prog.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLineService {

  private final OrderLineRepository orderLineRepository;
  private final OrderRepository orderRepository;
  private final CopyRepository copyRepository;

  @Transactional(readOnly = true)
  public List<OrderLineResponse> findAll() {
    return orderLineRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public OrderLineResponse findById(UUID id) {
    return orderLineRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("OrderLine not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<OrderLineResponse> findByOrderId(UUID orderId) {
    return orderLineRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public OrderLineResponse create(OrderLineRequest request) {
    var order =
        orderRepository
            .findById(request.orderId())
            .orElseThrow(
                () -> new EntityNotFoundException("Order not found: " + request.orderId()));
    var copy =
        copyRepository
            .findById(request.copyId())
            .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + request.copyId()));

    var line =
        OrderLine.builder()
            .order(order)
            .copy(copy)
            .quantity(request.quantity())
            .unitPrice(request.unitPrice() != null ? request.unitPrice() : copy.getPrice())
            .discount(request.discount() != null ? request.discount() : BigDecimal.ZERO)
            .build();
    return toResponse(orderLineRepository.save(line));
  }

  @Transactional
  public OrderLineResponse update(UUID id, OrderLineRequest request) {
    var line =
        orderLineRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("OrderLine not found: " + id));
    var order =
        orderRepository
            .findById(request.orderId())
            .orElseThrow(
                () -> new EntityNotFoundException("Order not found: " + request.orderId()));
    var copy =
        copyRepository
            .findById(request.copyId())
            .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + request.copyId()));

    line.setOrder(order);
    line.setCopy(copy);
    line.setQuantity(request.quantity());
    line.setUnitPrice(request.unitPrice() != null ? request.unitPrice() : copy.getPrice());
    line.setDiscount(request.discount() != null ? request.discount() : BigDecimal.ZERO);
    return toResponse(orderLineRepository.save(line));
  }

  @Transactional
  public void delete(UUID id) {
    if (!orderLineRepository.existsById(id)) {
      throw new EntityNotFoundException("OrderLine not found: " + id);
    }
    orderLineRepository.deleteById(id);
  }

  private OrderLineResponse toResponse(OrderLine line) {
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
