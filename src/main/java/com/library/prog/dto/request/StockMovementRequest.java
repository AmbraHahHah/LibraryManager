package com.library.prog.dto.request;

import com.library.prog.model.MovementTypeEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StockMovementRequest(
    @NotNull @Positive Integer quantity,
    @NotNull MovementTypeEnum movementType,
    @Size(max = 500) String reason,
    @NotNull UUID copyId,
    UUID orderId) {}
