package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.service.OrderLineService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-lines")
@RequiredArgsConstructor
public class OrderLineController {

  private final OrderLineService orderLineService;

  @GetMapping("/by-order")
  public List<OrderLineResponse> findByOrderId(@RequestParam UUID orderId) {
    return orderLineService.findByOrderId(orderId);
  }
}
