package com.library.prog.dto.response;

import com.library.prog.model.MovementTypeEnum;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StockMovementResponse(
    UUID id,
    Integer quantity,
    MovementTypeEnum movementType,
    String reason,
    Instant movementDate,
    UUID copyId,
    UUID orderId) {}
