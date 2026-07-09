package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.StockAdjustRequest;
import com.library.prog.dto.request.StockRequest;
import com.library.prog.model.*;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.StockMovementRepository;
import com.library.prog.repository.StockRepository;
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
class StockServiceTest {

  @Mock private StockRepository stockRepository;
  @Mock private StockMovementRepository stockMovementRepository;
  @Mock private CopyRepository copyRepository;

  @InjectMocks private StockService stockService;

  @Test
  void findAll_returns_all_stocks() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    when(stockRepository.findAll()).thenReturn(List.of(stock));

    var result = stockService.findAll();

    assertEquals(1, result.size());
    assertEquals(copy.getId(), result.getFirst().copyId());
    assertEquals(10, result.getFirst().availableQuantity());
    assertEquals(2, result.getFirst().reservedQuantity());
  }

  @Test
  void findById_returns_stock_when_found() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));

    var result = stockService.findById(stock.getId());

    assertEquals(stock.getId(), result.id());
    assertEquals(copy.getId(), result.copyId());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(stockRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.findById(id));
  }

  @Test
  void findByCopyId_returns_stock_when_found() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));

    var result = stockService.findByCopyId(copy.getId());

    assertEquals(stock.getId(), result.id());
    assertEquals(copy.getId(), result.copyId());
  }

  @Test
  void findByCopyId_throws_when_not_found() {
    var copyId = UUID.randomUUID();
    when(stockRepository.findByCopyId(copyId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.findByCopyId(copyId));
  }

  @Test
  void getLowStock_uses_default_threshold_when_none_provided() {
    var copy = buildCopy();
    var stock = buildStock(copy, 2, 0);
    when(stockRepository.findLowStock(StockService.DEFAULT_LOW_STOCK_THRESHOLD))
        .thenReturn(List.of(stock));

    var result = stockService.getLowStock(null);

    assertEquals(1, result.size());
    assertEquals(2, result.getFirst().availableStock());
    verify(stockRepository).findLowStock(3);
  }

  @Test
  void getLowStock_uses_provided_threshold() {
    var copy = buildCopy();
    var stock = buildStock(copy, 5, 0);
    when(stockRepository.findLowStock(10)).thenReturn(List.of(stock));

    var result = stockService.getLowStock(10);

    assertEquals(1, result.size());
    verify(stockRepository).findLowStock(10);
  }

  @Test
  void getLowStock_returns_empty_list_when_nothing_below_threshold() {
    when(stockRepository.findLowStock(anyInt())).thenReturn(List.of());

    var result = stockService.getLowStock(3);

    assertTrue(result.isEmpty());
  }

  @Test
  void getLowStock_marks_lowStock_flag_consistently_with_alertThreshold() {
    var copy = buildCopy();
    var lowStock = buildStock(copy, 1, 0);
    lowStock.setAlertThreshold(5);
    when(stockRepository.findLowStock(3)).thenReturn(List.of(lowStock));

    var result = stockService.getLowStock(3);

    assertTrue(result.getFirst().lowStock());
    assertEquals(1, result.getFirst().availableStock());
  }

  @Test
  void create_creates_stock_with_default_alert_threshold() {
    var copy = buildCopy();
    var request = StockRequest.builder().copyId(copy.getId()).build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.empty());
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.create(request);

    assertEquals(copy.getId(), result.copyId());
    assertEquals(0, result.availableQuantity());
    assertEquals(0, result.reservedQuantity());
    assertEquals(5, result.alertThreshold());
  }

  @Test
  void create_creates_stock_with_custom_alert_threshold() {
    var copy = buildCopy();
    var request = StockRequest.builder().copyId(copy.getId()).alertThreshold(10).build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.empty());
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.create(request);

    assertEquals(10, result.alertThreshold());
  }

  @Test
  void create_throws_when_copy_not_found() {
    var copyId = UUID.randomUUID();
    var request = StockRequest.builder().copyId(copyId).build();
    when(copyRepository.findById(copyId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.create(request));
  }

  @Test
  void create_throws_when_stock_already_exists() {
    var copy = buildCopy();
    var request = StockRequest.builder().copyId(copy.getId()).build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(stockRepository.findByCopyId(copy.getId()))
        .thenReturn(Optional.of(buildStock(copy, 5, 0)));

    assertThrows(IllegalStateException.class, () -> stockService.create(request));
  }

  @Test
  void update_modifies_alert_threshold() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request = StockRequest.builder().copyId(copy.getId()).alertThreshold(15).build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.update(stock.getId(), request);

    assertEquals(15, result.alertThreshold());
  }

  @Test
  void update_keeps_existing_alert_threshold_when_null() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    stock.setAlertThreshold(8);
    var request = StockRequest.builder().copyId(copy.getId()).build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.update(stock.getId(), request);

    assertEquals(8, result.alertThreshold());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = StockRequest.builder().copyId(UUID.randomUUID()).build();
    when(stockRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.update(id, request));
  }

  @Test
  void adjustStock_restock_increases_available_quantity() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(5)
            .movementType(MovementTypeEnum.RESTOCK)
            .reason("New shipment")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.adjustStock(stock.getId(), request);

    assertEquals(15, result.availableQuantity());
    assertEquals(2, result.reservedQuantity());
    assertEquals(13, result.availableStock());
  }

  @Test
  void adjustStock_sale_decreases_available_quantity() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(3)
            .movementType(MovementTypeEnum.SALE)
            .reason("Customer purchase")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.adjustStock(stock.getId(), request);

    assertEquals(7, result.availableQuantity());
    assertEquals(5, result.availableStock());
  }

  @Test
  void adjustStock_sale_throws_when_insufficient_stock() {
    var copy = buildCopy();
    var stock = buildStock(copy, 5, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(10)
            .movementType(MovementTypeEnum.SALE)
            .reason("Too many")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));

    assertThrows(
        IllegalStateException.class, () -> stockService.adjustStock(stock.getId(), request));
  }

  @Test
  void adjustStock_loss_reduces_quantity_when_sufficient() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(3)
            .movementType(MovementTypeEnum.LOSS)
            .reason("Damaged")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.adjustStock(stock.getId(), request);

    assertEquals(7, result.availableQuantity());
    assertEquals(2, result.reservedQuantity());
  }

  @Test
  void adjustStock_loss_zeroes_quantity_when_insufficient() {
    var copy = buildCopy();
    var stock = buildStock(copy, 3, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(10)
            .movementType(MovementTypeEnum.LOSS)
            .reason("Total loss")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.adjustStock(stock.getId(), request);

    assertEquals(0, result.availableQuantity());
    assertEquals(0, result.reservedQuantity());
  }

  @Test
  void adjustStock_customer_return_increases_available_quantity() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(2)
            .movementType(MovementTypeEnum.CUSTOMER_RETURN)
            .reason("Returned by customer")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(stockMovementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = stockService.adjustStock(stock.getId(), request);

    assertEquals(12, result.availableQuantity());
    assertEquals(10, result.availableStock());
  }

  @Test
  void adjustStock_creates_movement_record() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var request =
        StockAdjustRequest.builder()
            .quantity(5)
            .movementType(MovementTypeEnum.RESTOCK)
            .reason("Restock")
            .build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    var movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
    when(stockMovementRepository.save(movementCaptor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    stockService.adjustStock(stock.getId(), request);

    var movement = movementCaptor.getValue();
    assertEquals(5, movement.getQuantity());
    assertEquals(MovementTypeEnum.RESTOCK, movement.getMovementType());
    assertEquals("Restock", movement.getReason());
    assertEquals(copy, movement.getCopy());
    assertNull(movement.getOrder());
  }

  @Test
  void adjustStock_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request =
        StockAdjustRequest.builder().quantity(1).movementType(MovementTypeEnum.RESTOCK).build();
    when(stockRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.adjustStock(id, request));
  }

  @Test
  void getBookStock_aggregates_all_copies() {
    var book = buildBook();
    var copy1 = buildCopyWithBook(book);
    var copy2 = buildCopyWithBook(book);
    var stock1 = buildStock(copy1, 10, 2);
    var stock2 = buildStock(copy2, 5, 1);

    when(copyRepository.findByBookId(book.getId())).thenReturn(List.of(copy1, copy2));
    when(stockRepository.findByCopyId(copy1.getId())).thenReturn(Optional.of(stock1));
    when(stockRepository.findByCopyId(copy2.getId())).thenReturn(Optional.of(stock2));

    var result = stockService.getBookStock(book.getId());

    assertEquals(2, result.totalCopies());
    assertEquals(12, result.totalAvailableStock());
    assertEquals(3, result.totalReservedQuantity());
    assertFalse(result.outOfStock());
  }

  @Test
  void getBookStock_returns_outOfStock_when_no_stock() {
    var book = buildBook();
    var copy = buildCopyWithBook(book);
    var stock = buildStock(copy, 0, 0);

    when(copyRepository.findByBookId(book.getId())).thenReturn(List.of(copy));
    when(stockRepository.findByCopyId(copy.getId())).thenReturn(Optional.of(stock));

    var result = stockService.getBookStock(book.getId());

    assertTrue(result.outOfStock());
  }

  @Test
  void getBookStock_ignores_copies_without_stock() {
    var book = buildBook();
    var copy1 = buildCopyWithBook(book);
    var copy2 = buildCopyWithBook(book);
    var stock1 = buildStock(copy1, 10, 2);

    when(copyRepository.findByBookId(book.getId())).thenReturn(List.of(copy1, copy2));
    when(stockRepository.findByCopyId(copy1.getId())).thenReturn(Optional.of(stock1));
    when(stockRepository.findByCopyId(copy2.getId())).thenReturn(Optional.empty());

    var result = stockService.getBookStock(book.getId());

    assertEquals(2, result.totalCopies());
    assertEquals(8, result.totalAvailableStock());
    assertEquals(2, result.totalReservedQuantity());
  }

  @Test
  void getStockByEdition_groups_by_format() {
    var book = buildBook();
    var pbCopy = buildCopyWithBookAndFormat(book, FormatEnum.PAPERBACK);
    var hcCopy = buildCopyWithBookAndFormat(book, FormatEnum.HARDCOVER);
    var pbStock = buildStock(pbCopy, 10, 2);
    var hcStock = buildStock(hcCopy, 5, 1);

    when(copyRepository.findByBookId(book.getId())).thenReturn(List.of(pbCopy, hcCopy));
    when(stockRepository.findByCopyId(pbCopy.getId())).thenReturn(Optional.of(pbStock));
    when(stockRepository.findByCopyId(hcCopy.getId())).thenReturn(Optional.of(hcStock));

    var result = stockService.getStockByEdition(book.getId());

    assertEquals(2, result.editions().size());
    var pbEdition =
        result.editions().stream()
            .filter(e -> "PAPERBACK".equals(e.format()))
            .findFirst()
            .orElseThrow();
    assertEquals(1, pbEdition.copyCount());
    assertEquals(8, pbEdition.availableStock());
    assertEquals(2, pbEdition.reservedQuantity());

    var hcEdition =
        result.editions().stream()
            .filter(e -> "HARDCOVER".equals(e.format()))
            .findFirst()
            .orElseThrow();
    assertEquals(1, hcEdition.copyCount());
    assertEquals(4, hcEdition.availableStock());
    assertEquals(1, hcEdition.reservedQuantity());
  }

  @Test
  void getStockByEdition_returns_empty_when_no_copies() {
    var book = buildBook();
    when(copyRepository.findByBookId(book.getId())).thenReturn(List.of());

    var result = stockService.getStockByEdition(book.getId());

    assertTrue(result.editions().isEmpty());
  }

  @Test
  void delete_removes_stock_when_no_movements() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockMovementRepository.findByCopyIdOrderByMovementDateDesc(copy.getId()))
        .thenReturn(List.of());

    stockService.delete(stock.getId());

    verify(stockRepository).delete(stock);
  }

  @Test
  void delete_throws_when_stock_has_movements() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 2);
    var movement = StockMovement.builder().id(UUID.randomUUID()).quantity(1).build();
    when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
    when(stockMovementRepository.findByCopyIdOrderByMovementDateDesc(copy.getId()))
        .thenReturn(List.of(movement));

    assertThrows(IllegalStateException.class, () -> stockService.delete(stock.getId()));
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(stockRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> stockService.delete(id));
  }

  @Test
  void response_computes_available_stock_and_flags() {
    var copy = buildCopy();
    var stock = buildStock(copy, 10, 3);
    stock.setAlertThreshold(5);
    when(stockRepository.findAll()).thenReturn(List.of(stock));

    var result = stockService.findAll();

    assertEquals(7, result.getFirst().availableStock());
    assertFalse(result.getFirst().outOfStock());
    assertFalse(result.getFirst().lowStock());
  }

  @Test
  void response_marks_lowStock_when_below_threshold() {
    var copy = buildCopy();
    var stock = buildStock(copy, 6, 0);
    stock.setAlertThreshold(10);
    when(stockRepository.findAll()).thenReturn(List.of(stock));

    var result = stockService.findAll();

    assertTrue(result.getFirst().lowStock());
  }

  @Test
  void response_marks_outOfStock_when_available_is_zero() {
    var copy = buildCopy();
    var stock = buildStock(copy, 3, 3);
    when(stockRepository.findAll()).thenReturn(List.of(stock));

    var result = stockService.findAll();

    assertTrue(result.getFirst().outOfStock());
    assertEquals(0, result.getFirst().availableStock());
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

  private Copy buildCopy() {
    var book = buildBook();
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn("9782070612758")
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.TEN)
        .book(book)
        .build();
  }

  private Copy buildCopyWithBook(Book book) {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn(UUID.randomUUID().toString().substring(0, 13))
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.TEN)
        .book(book)
        .build();
  }

  private Copy buildCopyWithBookAndFormat(Book book, FormatEnum format) {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn(UUID.randomUUID().toString().substring(0, 13))
        .format(format)
        .price(BigDecimal.TEN)
        .book(book)
        .build();
  }

  private Book buildBook() {
    return Book.builder()
        .id(UUID.randomUUID())
        .title("Test Book")
        .language("English")
        .createdAt(Instant.now())
        .build();
  }
}
