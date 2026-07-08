package com.library.prog.repository;

import com.library.prog.dto.response.RevenueByGenreResponse;
import com.library.prog.model.OrderLine;
import com.library.prog.model.OrderStatusEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RevenueRepository extends JpaRepository<OrderLine, UUID> {

  @Query(
      """
      SELECT new com.library.prog.dto.response.RevenueByGenreResponse(
        c.name, SUM(ol.unitPrice * ol.quantity)
      )
      FROM OrderLine ol
      JOIN ol.copy cp
      JOIN cp.book b
      JOIN b.bookCategories bc
      JOIN bc.category c
      WHERE ol.order.status IN :statuses
      GROUP BY c.name
      ORDER BY SUM(ol.unitPrice * ol.quantity) DESC
      """)
  List<RevenueByGenreResponse> findRevenueByGenre(List<OrderStatusEnum> statuses);
}
