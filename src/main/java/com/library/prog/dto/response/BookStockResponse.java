package com.library.prog.dto.response;

import lombok.Builder;

@Builder
public record BookStockResponse(
    long totalCopies,
    int totalAvailableStock,
    int totalReservedQuantity,
    boolean outOfStock) {}
