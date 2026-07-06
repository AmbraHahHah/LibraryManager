package com.library.prog.service;

import com.library.prog.dto.request.StockMovementRequest;
import com.library.prog.dto.response.StockMovementResponse;
import com.library.prog.model.StockMovement;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.OrderRepository;
import com.library.prog.repository.StockMovementRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementService {

  private final StockMovementRepository stockMovementRepository;
  private final CopyRepository copyRepository;
  private final OrderRepository orderRepository;

  public List<StockMovementResponse> findAll() {
    return stockMovementRepository.findAll().stream().map(this::toResponse).toList();
  }

  public StockMovementResponse findById(UUID id) {
    return stockMovementRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("StockMovement not found: " + id));
  }

  public List<StockMovementResponse> findByCopyId(UUID copyId) {
    return stockMovementRepository
        .findByCopyIdOrderByMovementDateDesc(copyId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public List<StockMovementResponse> findByOrderId(UUID orderId) {
    return stockMovementRepository
        .findByOrderIdOrderByMovementDateDesc(orderId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public StockMovementResponse create(StockMovementRequest request) {
    var copy =
        copyRepository
            .findById(request.copyId())
            .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + request.copyId()));

    var order =
        request.orderId() != null
            ? orderRepository
                .findById(request.orderId())
                .orElseThrow(
                    () -> new EntityNotFoundException("Order not found: " + request.orderId()))
            : null;

    var movement =
        StockMovement.builder()
            .quantity(request.quantity())
            .movementType(request.movementType())
            .reason(request.reason())
            .copy(copy)
            .order(order)
            .build();

    return toResponse(stockMovementRepository.save(movement));
  }

  @Transactional
  public void delete(UUID id) {
    if (!stockMovementRepository.existsById(id)) {
      throw new EntityNotFoundException("StockMovement not found: " + id);
    }
    stockMovementRepository.deleteById(id);
  }

  private StockMovementResponse toResponse(StockMovement movement) {
    return StockMovementResponse.builder()
        .id(movement.getId())
        .quantity(movement.getQuantity())
        .movementType(movement.getMovementType())
        .reason(movement.getReason())
        .movementDate(movement.getMovementDate())
        .copyId(movement.getCopy().getId())
        .orderId(movement.getOrder() != null ? movement.getOrder().getId() : null)
        .build();
  }
}
