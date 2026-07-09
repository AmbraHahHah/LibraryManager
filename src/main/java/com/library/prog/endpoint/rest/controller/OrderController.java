package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.OrderRequest;
import com.library.prog.dto.response.OrderResponse;
import com.library.prog.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public List<OrderResponse> findAll() {
    return orderService.findAll();
  }

  @GetMapping("/{id}")
  public OrderResponse findById(@PathVariable UUID id) {
    return orderService.findById(id);
  }

  @PostMapping
  public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
    var response = orderService.create(request);
    return ResponseEntity.created(URI.create("/orders/" + response.id())).body(response);
  }

  @PatchMapping("/{id}/confirm")
  public OrderResponse confirm(@PathVariable UUID id) {
    return orderService.confirm(id);
  }

  @PatchMapping("/{id}/cancel")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancel(@PathVariable UUID id) {
    orderService.cancel(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    orderService.delete(id);
  }
}
