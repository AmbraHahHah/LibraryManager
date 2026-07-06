package com.library.prog.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StockResponse(
    UUID id,
    Integer availableQuantity,
    Integer reservedQuantity,
    Integer alertThreshold,
    int availableStock,
    boolean outOfStock,
    boolean lowStock,
    Instant lastUpdated,
    UUID copyId) {}
