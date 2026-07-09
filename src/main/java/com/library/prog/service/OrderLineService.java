package com.library.prog.service;

import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.model.OrderLine;
import com.library.prog.repository.OrderLineRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLineService {

  private final OrderLineRepository orderLineRepository;

  @Transactional(readOnly = true)
  public List<OrderLineResponse> findByOrderId(UUID orderId) {
    return orderLineRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
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
