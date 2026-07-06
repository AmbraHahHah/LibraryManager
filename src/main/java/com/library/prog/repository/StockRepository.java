package com.library.prog.repository;

import com.library.prog.model.Stock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
  Optional<Stock> findByCopyId(UUID copyId);
}
