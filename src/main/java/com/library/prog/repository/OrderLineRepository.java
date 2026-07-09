package com.library.prog.repository;

import com.library.prog.model.OrderLine;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {
  List<OrderLine> findByOrderId(UUID orderId);
}
