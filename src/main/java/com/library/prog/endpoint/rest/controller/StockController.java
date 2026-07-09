package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.StockAdjustRequest;
import com.library.prog.dto.request.StockRequest;
import com.library.prog.dto.response.StockResponse;
import com.library.prog.service.StockService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

  private final StockService stockService;

  @GetMapping
  public List<StockResponse> findAll() {
    return stockService.findAll();
  }

  @GetMapping("/{id}")
  public StockResponse findById(@PathVariable UUID id) {
    return stockService.findById(id);
  }

  @GetMapping("/by-copy")
  public StockResponse findByCopyId(@RequestParam UUID copyId) {
    return stockService.findByCopyId(copyId);
  }

  @PostMapping
  public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
    var response = stockService.create(request);
    return ResponseEntity.created(URI.create("/stocks/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public StockResponse update(@PathVariable UUID id, @Valid @RequestBody StockRequest request) {
    return stockService.update(id, request);
  }

  @PatchMapping("/{id}/adjust")
  public StockResponse adjustStock(
      @PathVariable UUID id, @Valid @RequestBody StockAdjustRequest request) {
    return stockService.adjustStock(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    stockService.delete(id);
  }
}
