package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.OrderLineRequest;
import com.library.prog.model.*;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.OrderLineRepository;
import com.library.prog.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceTest {

  @Mock private OrderLineRepository orderLineRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private CopyRepository copyRepository;

  @InjectMocks private OrderLineService orderLineService;

  @Test
  void findAll_returns_all_lines() {
    var line = buildOrderLine();
    when(orderLineRepository.findAll()).thenReturn(List.of(line));

    var result = orderLineService.findAll();

    assertEquals(1, result.size());
    assertEquals(line.getId(), result.getFirst().id());
  }

  @Test
  void findById_returns_line_when_found() {
    var line = buildOrderLine();
    when(orderLineRepository.findById(line.getId())).thenReturn(Optional.of(line));

    var result = orderLineService.findById(line.getId());

    assertEquals(line.getId(), result.id());
    assertEquals(3, result.quantity());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(orderLineRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderLineService.findById(id));
  }

  @Test
  void findByOrderId_returns_lines() {
    var orderId = UUID.randomUUID();
    var line = buildOrderLine();
    when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of(line));

    var result = orderLineService.findByOrderId(orderId);

    assertEquals(1, result.size());
    assertEquals(line.getId(), result.getFirst().id());
  }

  @Test
  void findByOrderId_returns_empty_when_no_lines() {
    var orderId = UUID.randomUUID();
    when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of());

    var result = orderLineService.findByOrderId(orderId);

    assertTrue(result.isEmpty());
  }

  @Test
  void create_saves_and_returns_line() {
    var order = buildOrder();
    var copy = buildCopy();
    var request =
        OrderLineRequest.builder()
            .orderId(order.getId())
            .copyId(copy.getId())
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(15))
            .discount(BigDecimal.ONE)
            .build();
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(orderLineRepository.save(any(OrderLine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = orderLineService.create(request);

    assertEquals(2, result.quantity());
    assertEquals(BigDecimal.valueOf(15), result.unitPrice());
    assertEquals(BigDecimal.ONE, result.discount());
    assertEquals(copy.getId(), result.copyId());
  }

  @Test
  void create_uses_copy_price_when_unitPrice_null() {
    var order = buildOrder();
    var copy = buildCopy();
    copy.setPrice(BigDecimal.valueOf(20));
    var request =
        OrderLineRequest.builder().orderId(order.getId()).copyId(copy.getId()).quantity(1).build();
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(orderLineRepository.save(any(OrderLine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = orderLineService.create(request);

    assertEquals(BigDecimal.valueOf(20), result.unitPrice());
    assertEquals(BigDecimal.ZERO, result.discount());
  }

  @Test
  void create_throws_when_order_not_found() {
    var orderId = UUID.randomUUID();
    var request =
        OrderLineRequest.builder().orderId(orderId).copyId(UUID.randomUUID()).quantity(1).build();
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderLineService.create(request));
  }

  @Test
  void create_throws_when_copy_not_found() {
    var order = buildOrder();
    var copyId = UUID.randomUUID();
    var request =
        OrderLineRequest.builder().orderId(order.getId()).copyId(copyId).quantity(1).build();
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(copyRepository.findById(copyId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderLineService.create(request));
  }

  @Test
  void update_modifies_existing_line() {
    var existing = buildOrderLine();
    var newOrder = buildOrder();
    var newCopy = buildCopy();
    var request =
        OrderLineRequest.builder()
            .orderId(newOrder.getId())
            .copyId(newCopy.getId())
            .quantity(5)
            .unitPrice(BigDecimal.valueOf(25))
            .discount(BigDecimal.valueOf(2))
            .build();
    when(orderLineRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(orderRepository.findById(newOrder.getId())).thenReturn(Optional.of(newOrder));
    when(copyRepository.findById(newCopy.getId())).thenReturn(Optional.of(newCopy));
    when(orderLineRepository.save(any(OrderLine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = orderLineService.update(existing.getId(), request);

    assertEquals(5, result.quantity());
    assertEquals(BigDecimal.valueOf(25), result.unitPrice());
    assertEquals(BigDecimal.valueOf(2), result.discount());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request =
        OrderLineRequest.builder()
            .orderId(UUID.randomUUID())
            .copyId(UUID.randomUUID())
            .quantity(1)
            .build();
    when(orderLineRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> orderLineService.update(id, request));
  }

  @Test
  void delete_removes_line_when_exists() {
    var id = UUID.randomUUID();
    when(orderLineRepository.existsById(id)).thenReturn(true);

    orderLineService.delete(id);

    verify(orderLineRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(orderLineRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> orderLineService.delete(id));
  }

  private OrderLine buildOrderLine() {
    return OrderLine.builder()
        .id(UUID.randomUUID())
        .order(buildOrder())
        .copy(buildCopy())
        .quantity(3)
        .unitPrice(BigDecimal.TEN)
        .discount(BigDecimal.ZERO)
        .build();
  }

  private Order buildOrder() {
    return Order.builder().id(UUID.randomUUID()).build();
  }

  private Copy buildCopy() {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn("9782070612758")
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.TEN)
        .build();
  }
}
