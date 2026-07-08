package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.prog.dto.response.RevenueByGenreResponse;
import com.library.prog.model.OrderStatusEnum;
import com.library.prog.repository.RevenueRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

  @Mock private RevenueRepository revenueRepository;

  @InjectMocks private RevenueService revenueService;

  @Test
  void getRevenueByGenre_returns_revenue_by_genre() {
    var expected =
        List.of(
            new RevenueByGenreResponse("Romance", new BigDecimal("730000")),
            new RevenueByGenreResponse("Science Fiction", new BigDecimal("450000")));
    when(revenueRepository.findRevenueByGenre(anyList())).thenReturn(expected);

    var result = revenueService.getRevenueByGenre();

    assertEquals(2, result.size());
    assertEquals("Romance", result.getFirst().genre());
    assertEquals(0, new BigDecimal("730000").compareTo(result.getFirst().revenue()));
  }

  @Test
  void getRevenueByGenre_filters_only_active_statuses() {
    revenueService.getRevenueByGenre();

    var captor = ArgumentCaptor.<List<OrderStatusEnum>>captor();
    verify(revenueRepository).findRevenueByGenre(captor.capture());

    var statuses = captor.getValue();
    assertTrue(statuses.contains(OrderStatusEnum.CONFIRMED));
    assertTrue(statuses.contains(OrderStatusEnum.PREPARING));
    assertTrue(statuses.contains(OrderStatusEnum.SHIPPED));
    assertTrue(statuses.contains(OrderStatusEnum.DELIVERED));
    assertFalse(statuses.contains(OrderStatusEnum.PENDING));
    assertFalse(statuses.contains(OrderStatusEnum.CANCELLED));
    assertFalse(statuses.contains(OrderStatusEnum.REFUNDED));
  }

  @Test
  void getRevenueByGenre_returns_empty_when_no_revenue() {
    when(revenueRepository.findRevenueByGenre(anyList())).thenReturn(List.of());

    var result = revenueService.getRevenueByGenre();

    assertTrue(result.isEmpty());
  }

  @Test
  void getRevenueByGenre_returns_sorted_by_revenue_descending() {
    var expected =
        List.of(
            new RevenueByGenreResponse("Romance", new BigDecimal("900000")),
            new RevenueByGenreResponse("Fantasy", new BigDecimal("500000")),
            new RevenueByGenreResponse("Mystery", new BigDecimal("300000")));
    when(revenueRepository.findRevenueByGenre(anyList())).thenReturn(expected);

    var result = revenueService.getRevenueByGenre();

    assertEquals(3, result.size());
    assertTrue(result.get(0).revenue().compareTo(result.get(1).revenue()) >= 0);
    assertTrue(result.get(1).revenue().compareTo(result.get(2).revenue()) >= 0);
  }
}
