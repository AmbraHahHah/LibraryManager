package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.library.prog.model.Book;
import com.library.prog.model.Copy;
import com.library.prog.model.FormatEnum;
import com.library.prog.model.Stock;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.StockMovementRepository;
import com.library.prog.repository.StockRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  void getLowStock_uses_default_threshold_when_none_provided() {
    var stock = buildStock(2, 0);
    when(stockRepository.findLowStock(StockService.DEFAULT_LOW_STOCK_THRESHOLD))
        .thenReturn(List.of(stock));

    var result = stockService.getLowStock(null);

    assertEquals(1, result.size());
    assertEquals(2, result.getFirst().availableStock());
    verify(stockRepository).findLowStock(3);
  }

  @Test
  void getLowStock_uses_provided_threshold() {
    var stock = buildStock(5, 0);
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
    var lowStock = buildStock(1, 0);
    lowStock.setAlertThreshold(5);
    when(stockRepository.findLowStock(3)).thenReturn(List.of(lowStock));

    var result = stockService.getLowStock(3);

    assertTrue(result.getFirst().lowStock());
    assertEquals(1, result.getFirst().availableStock());
  }

  private Stock buildStock(int available, int reserved) {
    var book = Book.builder().id(UUID.randomUUID()).title("Test Book").build();
    var copy =
        Copy.builder()
            .id(UUID.randomUUID())
            .isbn("1234567890")
            .format(FormatEnum.PAPERBACK)
            .book(book)
            .build();
    return Stock.builder()
        .id(UUID.randomUUID())
        .availableQuantity(available)
        .reservedQuantity(reserved)
        .alertThreshold(5)
        .copy(copy)
        .build();
  }
}