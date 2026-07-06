package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.response.RevenueByGenreResponse;
import com.library.prog.service.RevenueService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

  private final RevenueService revenueService;

  @GetMapping("/by-genre")
  public List<RevenueByGenreResponse> getRevenueByGenre() {
    return revenueService.getRevenueByGenre();
  }
}
