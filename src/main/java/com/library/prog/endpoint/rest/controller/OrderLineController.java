package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.OrderLineRequest;
import com.library.prog.dto.response.OrderLineResponse;
import com.library.prog.service.OrderLineService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-lines")
@RequiredArgsConstructor
public class OrderLineController {

  private final OrderLineService orderLineService;

  @GetMapping
  public List<OrderLineResponse> findAll() {
    return orderLineService.findAll();
  }

  @GetMapping("/{id}")
  public OrderLineResponse findById(@PathVariable UUID id) {
    return orderLineService.findById(id);
  }

  @GetMapping("/by-order")
  public List<OrderLineResponse> findByOrderId(@RequestParam UUID orderId) {
    return orderLineService.findByOrderId(orderId);
  }

  @PostMapping
  public ResponseEntity<OrderLineResponse> create(@Valid @RequestBody OrderLineRequest request) {
    var response = orderLineService.create(request);
    return ResponseEntity.created(URI.create("/order-lines/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public OrderLineResponse update(
      @PathVariable UUID id, @Valid @RequestBody OrderLineRequest request) {
    return orderLineService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    orderLineService.delete(id);
  }
}
