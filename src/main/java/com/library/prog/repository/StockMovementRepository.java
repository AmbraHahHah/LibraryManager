package com.library.prog.repository;

import com.library.prog.model.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
  List<StockMovement> findByCopyIdOrderByMovementDateDesc(UUID copyId);

  List<StockMovement> findByOrderIdOrderByMovementDateDesc(UUID orderId);
}
