package com.library.prog.service;

import com.library.prog.dto.response.RevenueByGenreResponse;
import com.library.prog.model.OrderStatusEnum;
import com.library.prog.repository.RevenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RevenueService {

  private final RevenueRepository revenueRepository;

  public List<RevenueByGenreResponse> getRevenueByGenre() {
    var statuses =
        List.of(
            OrderStatusEnum.CONFIRMED,
            OrderStatusEnum.PREPARING,
            OrderStatusEnum.SHIPPED,
            OrderStatusEnum.DELIVERED);
    return revenueRepository.findRevenueByGenre(statuses);
  }
}
