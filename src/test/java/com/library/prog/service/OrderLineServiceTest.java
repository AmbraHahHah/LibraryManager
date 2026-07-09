package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.model.*;
import com.library.prog.repository.OrderLineRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceTest {

  @Mock private OrderLineRepository orderLineRepository;

  @InjectMocks private OrderLineService orderLineService;

  @Test
  void findByOrderId_returns_lines() {
    var orderId = UUID.randomUUID();
    var copy =
        Copy.builder()
            .id(UUID.randomUUID())
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .build();
    var line =
        OrderLine.builder()
            .id(UUID.randomUUID())
            .copy(copy)
            .quantity(3)
            .unitPrice(BigDecimal.TEN)
            .discount(BigDecimal.ZERO)
            .build();
    when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of(line));

    var result = orderLineService.findByOrderId(orderId);

    assertEquals(1, result.size());
    var res = result.getFirst();
    assertEquals(line.getId(), res.id());
    assertEquals(copy.getId(), res.copyId());
    assertEquals("9782070612758", res.isbn());
    assertEquals(FormatEnum.PAPERBACK, res.format());
    assertEquals(3, res.quantity());
  }

  @Test
  void findByOrderId_returns_empty_when_no_lines() {
    var orderId = UUID.randomUUID();
    when(orderLineRepository.findByOrderId(orderId)).thenReturn(List.of());

    var result = orderLineService.findByOrderId(orderId);

    assertTrue(result.isEmpty());
  }
}
