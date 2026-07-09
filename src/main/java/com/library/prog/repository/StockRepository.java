package com.library.prog.repository;

import com.library.prog.model.Stock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
  Optional<Stock> findByCopyId(UUID copyId);

  @Query(
      "SELECT s FROM Stock s WHERE (s.availableQuantity - s.reservedQuantity) <= :threshold "
          + "ORDER BY (s.availableQuantity - s.reservedQuantity) ASC")
  List<Stock> findLowStock(@Param("threshold") int threshold);
}