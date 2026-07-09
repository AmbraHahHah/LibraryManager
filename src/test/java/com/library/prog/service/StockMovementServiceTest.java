package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.StockMovementRequest;
import com.library.prog.model.*;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.OrderRepository;
import com.library.prog.repository.StockMovementRepository;
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
class StockMovementServiceTest {

  @Mock private StockMovementRepository stockMovementRepository;
  @Mock private CopyRepository copyRepository;
  @Mock private OrderRepository orderRepository;

  @InjectMocks private StockMovementService stockMovementService;

  @Test
  void findAll_returns_all_movements() {
    var copy = buildCopy();
    var movement = buildMovement(copy, null);
    when(stockMovementRepository.findAll()).thenReturn(List.of(movement));

    var result = stockMovementService.findAll();

    assertEquals(1, result.size());
    assertEquals(copy.getId(), result.getFirst().copyId());
    assertNull(result.getFirst().orderId());
  }

  @Test
  void findById_returns_movement_when_found() {
    var copy = buildCopy();
    var movement = buildMovement(copy, null);
    when(stockMovementRepository.findById(movement.getId())).thenReturn(Optional.of(movement));

    var result = stockMovementService.findById(movement.getId());

    assertEquals(movement.getId(), result.id());
    assertEquals(movement.getQuantity(), result.quantity());
    assertEquals(movement.getMovementType(), result.movementType());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(stockMovementRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockMovementService.findById(id));
  }

  @Test
  void findByCopyId_returns_movements_for_copy() {
    var copy = buildCopy();
    var movement = buildMovement(copy, null);
    when(stockMovementRepository.findByCopyIdOrderByMovementDateDesc(copy.getId()))
        .thenReturn(List.of(movement));

    var result = stockMovementService.findByCopyId(copy.getId());

    assertEquals(1, result.size());
    assertEquals(copy.getId(), result.getFirst().copyId());
  }

  @Test
  void findByCopyId_returns_empty_when_no_movements() {
    var copyId = UUID.randomUUID();
    when(stockMovementRepository.findByCopyIdOrderByMovementDateDesc(copyId)).thenReturn(List.of());

    var result = stockMovementService.findByCopyId(copyId);

    assertTrue(result.isEmpty());
  }

  @Test
  void findByOrderId_returns_movements_for_order() {
    var order = buildOrder();
    var copy = buildCopy();
    var movement = buildMovement(copy, order);
    when(stockMovementRepository.findByOrderIdOrderByMovementDateDesc(order.getId()))
        .thenReturn(List.of(movement));

    var result = stockMovementService.findByOrderId(order.getId());

    assertEquals(1, result.size());
    assertEquals(order.getId(), result.getFirst().orderId());
  }

  @Test
  void findByOrderId_returns_empty_when_no_movements() {
    var orderId = UUID.randomUUID();
    when(stockMovementRepository.findByOrderIdOrderByMovementDateDesc(orderId))
        .thenReturn(List.of());

    var result = stockMovementService.findByOrderId(orderId);

    assertTrue(result.isEmpty());
  }

  @Test
  void create_creates_movement_without_order() {
    var copy = buildCopy();
    var request =
        StockMovementRequest.builder()
            .quantity(5)
            .movementType(MovementTypeEnum.RESTOCK)
            .reason("Restock")
            .copyId(copy.getId())
            .build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockMovementService.create(request);

    assertEquals(5, result.quantity());
    assertEquals(MovementTypeEnum.RESTOCK, result.movementType());
    assertEquals("Restock", result.reason());
    assertEquals(copy.getId(), result.copyId());
    assertNull(result.orderId());
  }

  @Test
  void create_creates_movement_with_order() {
    var copy = buildCopy();
    var order = buildOrder();
    var request =
        StockMovementRequest.builder()
            .quantity(3)
            .movementType(MovementTypeEnum.SALE)
            .reason("Sale")
            .copyId(copy.getId())
            .orderId(order.getId())
            .build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockMovementService.create(request);

    assertEquals(3, result.quantity());
    assertEquals(MovementTypeEnum.SALE, result.movementType());
    assertEquals(copy.getId(), result.copyId());
    assertEquals(order.getId(), result.orderId());
  }

  @Test
  void create_throws_when_copy_not_found() {
    var copyId = UUID.randomUUID();
    var request =
        StockMovementRequest.builder()
            .quantity(1)
            .movementType(MovementTypeEnum.RESTOCK)
            .copyId(copyId)
            .build();
    when(copyRepository.findById(copyId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockMovementService.create(request));
  }

  @Test
  void create_throws_when_order_not_found() {
    var copy = buildCopy();
    var orderId = UUID.randomUUID();
    var request =
        StockMovementRequest.builder()
            .quantity(1)
            .movementType(MovementTypeEnum.SALE)
            .copyId(copy.getId())
            .orderId(orderId)
            .build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockMovementService.create(request));
  }

  @Test
  void create_saves_with_correct_fields() {
    var copy = buildCopy();
    var request =
        StockMovementRequest.builder()
            .quantity(10)
            .movementType(MovementTypeEnum.LOSS)
            .reason("Damaged goods")
            .copyId(copy.getId())
            .build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    var captor = ArgumentCaptor.forClass(StockMovement.class);
    when(stockMovementRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    stockMovementService.create(request);

    var saved = captor.getValue();
    assertEquals(10, saved.getQuantity());
    assertEquals(MovementTypeEnum.LOSS, saved.getMovementType());
    assertEquals("Damaged goods", saved.getReason());
    assertEquals(copy, saved.getCopy());
    assertNull(saved.getOrder());
  }

  @Test
  void delete_removes_movement_when_exists() {
    var id = UUID.randomUUID();
    when(stockMovementRepository.existsById(id)).thenReturn(true);

    stockMovementService.delete(id);

    verify(stockMovementRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(stockMovementRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> stockMovementService.delete(id));
  }

  private StockMovement buildMovement(Copy copy, Order order) {
    return StockMovement.builder()
        .id(UUID.randomUUID())
        .quantity(5)
        .movementType(MovementTypeEnum.RESTOCK)
        .reason("Test movement")
        .movementDate(Instant.now())
        .copy(copy)
        .order(order)
        .build();
  }

  private Copy buildCopy() {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn("9782070612758")
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.TEN)
        .book(
            Book.builder()
                .id(UUID.randomUUID())
                .title("Test Book")
                .language("English")
                .createdAt(Instant.now())
                .build())
        .build();
  }

  private Order buildOrder() {
    return Order.builder().id(UUID.randomUUID()).totalAmount(BigDecimal.TEN).build();
  }
}
