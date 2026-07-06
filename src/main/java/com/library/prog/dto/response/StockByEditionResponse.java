package com.library.prog.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record StockByEditionResponse(
    List<EditionStock> editions) {

  @Builder
  public record EditionStock(
      String format,
      long copyCount,
      int availableStock,
      int reservedQuantity) {}
}
