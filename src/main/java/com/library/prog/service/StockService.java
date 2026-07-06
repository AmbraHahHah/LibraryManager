package com.library.prog.service;

import com.library.prog.dto.request.StockAdjustRequest;
import com.library.prog.dto.request.StockRequest;
import com.library.prog.dto.response.BookStockResponse;
import com.library.prog.dto.response.StockByEditionResponse;
import com.library.prog.dto.response.StockByEditionResponse.EditionStock;
import com.library.prog.dto.response.StockResponse;
import com.library.prog.model.Stock;
import com.library.prog.model.StockMovement;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.StockMovementRepository;
import com.library.prog.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;
  private final StockMovementRepository stockMovementRepository;
  private final CopyRepository copyRepository;

  public List<StockResponse> findAll() {
    return stockRepository.findAll().stream().map(this::toResponse).toList();
  }

  public StockResponse findById(UUID id) {
    return stockRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Stock not found: " + id));
  }

  public StockResponse findByCopyId(UUID copyId) {
    return stockRepository
        .findByCopyId(copyId)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Stock not found for copy: " + copyId));
  }

  @Transactional
  public StockResponse create(@Valid StockRequest request) {
    var copy =
        copyRepository
            .findById(request.copyId())
            .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + request.copyId()));

    if (stockRepository.findByCopyId(request.copyId()).isPresent()) {
      throw new IllegalStateException("Stock already exists for copy: " + request.copyId());
    }

    var stock =
        Stock.builder()
            .availableQuantity(0)
            .reservedQuantity(0)
            .alertThreshold(request.alertThreshold() != null ? request.alertThreshold() : 5)
            .copy(copy)
            .build();

    return toResponse(stockRepository.save(stock));
  }

  @Transactional
  public StockResponse update(UUID id, @Valid StockRequest request) {
    var stock =
        stockRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Stock not found: " + id));

    if (request.alertThreshold() != null) {
      stock.setAlertThreshold(request.alertThreshold());
    }

    return toResponse(stockRepository.save(stock));
  }

  @Transactional
  public StockResponse adjustStock(UUID id, @Valid StockAdjustRequest request) {
    var stock =
        stockRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Stock not found: " + id));

    switch (request.movementType()) {
      case RESTOCK -> {
        stock.setAvailableQuantity(stock.getAvailableQuantity() + request.quantity());
        stock.setReservedQuantity(stock.getReservedQuantity());
      }
      case SALE -> {
        if (stock.getAvailableStock() < request.quantity()) {
          throw new IllegalStateException(
              "Insufficient stock: available "
                  + stock.getAvailableStock()
                  + ", requested "
                  + request.quantity());
        }
        stock.setAvailableQuantity(stock.getAvailableQuantity() - request.quantity());
      }
      case LOSS -> {
        if (stock.getAvailableStock() < request.quantity()) {
          stock.setAvailableQuantity(0);
          stock.setReservedQuantity(0);
        } else {
          stock.setAvailableQuantity(stock.getAvailableQuantity() - request.quantity());
        }
      }
      case CUSTOMER_RETURN ->
          stock.setAvailableQuantity(stock.getAvailableQuantity() + request.quantity());
    }

    stockRepository.save(stock);

    var movement =
        StockMovement.builder()
            .quantity(request.quantity())
            .movementType(request.movementType())
            .reason(request.reason())
            .copy(stock.getCopy())
            .build();

    stockMovementRepository.save(movement);

    return toResponse(stock);
  }

  public BookStockResponse getBookStock(UUID bookId) {
    var copies = copyRepository.findByBookId(bookId);
    var stocks = copies.stream().map(c -> stockRepository.findByCopyId(c.getId()));

    long totalCopies = copies.size();
    int totalAvailableStock = 0;
    int totalReservedQuantity = 0;

    for (var optStock : stocks.toList()) {
      if (optStock.isPresent()) {
        var s = optStock.get();
        totalAvailableStock += s.getAvailableStock();
        totalReservedQuantity += s.getReservedQuantity();
      }
    }

    return BookStockResponse.builder()
        .totalCopies(totalCopies)
        .totalAvailableStock(totalAvailableStock)
        .totalReservedQuantity(totalReservedQuantity)
        .outOfStock(totalAvailableStock <= 0)
        .build();
  }

  public StockByEditionResponse getStockByEdition(UUID bookId) {
    var copies = copyRepository.findByBookId(bookId);

    var editionMap =
        copies.stream()
            .collect(Collectors.groupingBy(
                c -> c.getFormat().name(),
                Collectors.collectingAndThen(Collectors.toList(), editionCopies -> {
                  long copyCount = editionCopies.size();
                  int availableStock = 0;
                  int reservedQty = 0;
                  for (var c : editionCopies) {
                    var optStock = stockRepository.findByCopyId(c.getId());
                    if (optStock.isPresent()) {
                      var s = optStock.get();
                      availableStock += s.getAvailableStock();
                      reservedQty += s.getReservedQuantity();
                    }
                  }
                  return EditionStock.builder()
                      .format(editionCopies.getFirst().getFormat().name())
                      .copyCount(copyCount)
                      .availableStock(availableStock)
                      .reservedQuantity(reservedQty)
                      .build();
                })));

    return StockByEditionResponse.builder()
        .editions(List.copyOf(editionMap.values()))
        .build();
  }

  @Transactional
  public void delete(UUID id) {
    var stock =
        stockRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Stock not found: " + id));

    var movements =
        stockMovementRepository.findByCopyIdOrderByMovementDateDesc(stock.getCopy().getId());
    if (!movements.isEmpty()) {
      throw new IllegalStateException("Cannot delete stock with existing movements");
    }

    stockRepository.delete(stock);
  }

  private StockResponse toResponse(Stock stock) {
    return StockResponse.builder()
        .id(stock.getId())
        .availableQuantity(stock.getAvailableQuantity())
        .reservedQuantity(stock.getReservedQuantity())
        .alertThreshold(stock.getAlertThreshold())
        .availableStock(stock.getAvailableStock())
        .outOfStock(stock.isOutOfStock())
        .lowStock(stock.isLowStock())
        .lastUpdated(stock.getLastUpdated())
        .copyId(stock.getCopy().getId())
        .build();
  }
}
