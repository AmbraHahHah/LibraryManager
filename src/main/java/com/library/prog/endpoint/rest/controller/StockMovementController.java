package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.StockMovementRequest;
import com.library.prog.dto.response.StockMovementResponse;
import com.library.prog.service.StockMovementService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

  private final StockMovementService stockMovementService;

  @GetMapping
  public List<StockMovementResponse> findAll() {
    return stockMovementService.findAll();
  }

  @GetMapping("/{id}")
  public StockMovementResponse findById(@PathVariable UUID id) {
    return stockMovementService.findById(id);
  }

  @GetMapping("/by-copy")
  public List<StockMovementResponse> findByCopyId(@RequestParam UUID copyId) {
    return stockMovementService.findByCopyId(copyId);
  }

  @GetMapping("/by-order")
  public List<StockMovementResponse> findByOrderId(@RequestParam UUID orderId) {
    return stockMovementService.findByOrderId(orderId);
  }

  @PostMapping
  public ResponseEntity<StockMovementResponse> create(
      @Valid @RequestBody StockMovementRequest request) {
    var response = stockMovementService.create(request);
    return ResponseEntity.created(URI.create("/stock-movements/" + response.id())).body(response);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    stockMovementService.delete(id);
  }
}
