package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.OrderLineRequest;
import com.library.prog.dto.request.OrderRequest;
import com.library.prog.model.*;
import com.library.prog.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderLineRepository orderLineRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private CopyRepository copyRepository;
  @Mock private StockRepository stockRepository;
  @Mock private StockMovementRepository stockMovementRepository;

  @InjectMocks private OrderService orderService;

  @Test
  void findAll_returns_all_orders() {
    var client = buildClient();
    var order = buildOrder(client, OrderStatusEnum.PENDING);
    when(orderRepository.findAll()).thenReturn(List.of(order));

    var result = orderService.findAll();

    assertEquals(1, result.size());
    assertEquals(order.getId(), result.getFirst().id());
    assertEquals(client.getId(), result.getFirst().clientId());
  }

  @Test
  void findById_returns_order_when_found() {
    var client = buildClient();
    var order = buildOrder(client, OrderStatusEnum.PENDING);
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    var result = orderService.findById(order.getId());

    assertEquals(order.getId(), result.id());
    assertEquals(client.getId(), result.clientId());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.findById(id));
  }

  @Test
  void create_creates_order_and_reserves_stock() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 0);
    var lineReq = OrderLineRequest.builder().copyId(copy.getId()).quantity(3).build();
    var request =
        OrderRequest.builder()
            .clientId(client.getId())
            .shippingAddress("123 Rue Test")
            .lines(List.of(lineReq))
            .build();

    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            inv -> {
              var o = (Order) inv.getArgument(0);
              if (o.getId() == null) o.setId(UUID.randomUUID());
              return o;
            });
    when(orderLineRepository.save(any(OrderLine.class))).thenAnswer(inv -> inv.getArgument(0));
    when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

    var result = orderService.create(request);

    assertNotNull(result.id());
    assertEquals(client.getId(), result.clientId());
    assertEquals(OrderStatusEnum.PENDING, result.status());
    assertEquals(1, result.lines().size());

    assertEquals(3, stock.getReservedQuantity());
    assertEquals(10, stock.getAvailableQuantity());
  }

  @Test
  void create_throws_when_client_not_found() {
    var clientId = UUID.randomUUID();
    var request =
        OrderRequest.builder()
            .clientId(clientId)
            .lines(
                List.of(OrderLineRequest.builder().copyId(UUID.randomUUID()).quantity(1).build()))
            .build();

    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.create(request));
  }

  @Test
  void create_throws_when_copy_not_found() {
    var client = buildClient();
    var copyId = UUID.randomUUID();
    var request =
        OrderRequest.builder()
            .clientId(client.getId())
            .lines(List.of(OrderLineRequest.builder().copyId(copyId).quantity(1).build()))
            .build();

    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
    when(copyRepository.findById(copyId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.create(request));
  }

  @Test
  void create_throws_when_stock_not_found() {
    var client = buildClient();
    var copy = buildCopy();
    var request =
        OrderRequest.builder()
            .clientId(client.getId())
            .lines(List.of(OrderLineRequest.builder().copyId(copy.getId()).quantity(1).build()))
            .build();

    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.create(request));
  }

  @Test
  void create_throws_when_insufficient_stock() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 2, 0);
    var request =
        OrderRequest.builder()
            .clientId(client.getId())
            .lines(List.of(OrderLineRequest.builder().copyId(copy.getId()).quantity(5).build()))
            .build();

    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));

    assertThrows(IllegalStateException.class, () -> orderService.create(request));
  }

  @Test
  void confirm_deducts_stock_and_creates_movement() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 3);
    var orderLine =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .order(Order.builder().id(UUID.randomUUID()).build())
            .copy(copy)
            .quantity(3)
            .unitPrice(BigDecimal.TEN)
            .build();
    var order = buildOrderWithLines(client, OrderStatusEnum.PENDING, List.of(orderLine));

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    var result = orderService.confirm(order.getId());

    assertEquals(OrderStatusEnum.CONFIRMED, result.status());
    assertEquals(7, stock.getAvailableQuantity());
    assertEquals(0, stock.getReservedQuantity());

    var movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
    verify(stockMovementRepository).save(movementCaptor.capture());
    assertEquals(MovementTypeEnum.SALE, movementCaptor.getValue().getMovementType());
    assertEquals(3, movementCaptor.getValue().getQuantity());
  }

  @Test
  void confirm_throws_when_not_pending() {
    var client = buildClient();
    var order = buildOrder(client, OrderStatusEnum.CONFIRMED);
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService.confirm(order.getId()));
  }

  @Test
  void cancel_pending_order_releases_reserved_stock() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 3);
    var orderLine =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .copy(copy)
            .quantity(3)
            .unitPrice(BigDecimal.TEN)
            .build();
    var order = buildOrderWithLines(client, OrderStatusEnum.PENDING, List.of(orderLine));

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

    orderService.cancel(order.getId());

    assertEquals(0, stock.getReservedQuantity());
    assertEquals(10, stock.getAvailableQuantity());
    verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatusEnum.CANCELLED));
  }

  @Test
  void cancel_confirmed_order_adds_back_to_available() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 7, 0);
    var orderLine =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .copy(copy)
            .quantity(3)
            .unitPrice(BigDecimal.TEN)
            .build();
    var order = buildOrderWithLines(client, OrderStatusEnum.CONFIRMED, List.of(orderLine));

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

    orderService.cancel(order.getId());

    assertEquals(10, stock.getAvailableQuantity());
    verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatusEnum.CANCELLED));
  }

  @Test
  void cancel_throws_when_already_cancelled() {
    var client = buildClient();
    var order = buildOrder(client, OrderStatusEnum.CANCELLED);
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService.cancel(order.getId()));
  }

  @Test
  void cancel_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.cancel(id));
  }

  @Test
  void delete_removes_pending_order_and_releases_stock() {
    var client = buildClient();
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 3);
    var orderLine =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .copy(copy)
            .quantity(3)
            .unitPrice(BigDecimal.TEN)
            .build();
    var order = buildOrderWithLines(client, OrderStatusEnum.PENDING, List.of(orderLine));

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

    orderService.delete(order.getId());

    assertEquals(0, stock.getReservedQuantity());
    verify(orderRepository).delete(order);
  }

  @Test
  void delete_throws_when_not_pending() {
    var client = buildClient();
    var order = buildOrder(client, OrderStatusEnum.CONFIRMED);
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    assertThrows(IllegalStateException.class, () -> orderService.delete(order.getId()));
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderService.delete(id));
  }

  private Client buildClient() {
    return Client.builder()
        .id(UUID.randomUUID())
        .lastName("Dupont")
        .firstName("Jean")
        .email("jean@test.com")
        .build();
  }

  private Copy buildCopy() {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn("9782070612758")
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.TEN)
        .book(Book.builder().id(UUID.randomUUID()).title("Test").build())
        .build();
  }

  private Stock buildStock(Copy copy, int available, int reserved) {
    return Stock.builder()
        .id(UUID.randomUUID())
        .availableQuantity(available)
        .reservedQuantity(reserved)
        .alertThreshold(5)
        .lastUpdated(Instant.now())
        .copy(copy)
        .build();
  }

  private Order buildOrder(Client client, OrderStatusEnum status) {
    return Order.builder()
        .id(UUID.randomUUID())
        .customer(client)
        .status(status)
        .totalAmount(BigDecimal.TEN)
        .shippingFee(BigDecimal.ZERO)
        .orderDate(Instant.now())
        .orderLines(List.of())
        .build();
  }

  private Order buildOrderWithLines(Client client, OrderStatusEnum status, List<OrderLine> lines) {
    return Order.builder()
        .id(UUID.randomUUID())
        .customer(client)
        .status(status)
        .totalAmount(BigDecimal.TEN)
        .shippingFee(BigDecimal.ZERO)
        .orderDate(Instant.now())
        .orderLines(lines)
        .build();
  }
}
