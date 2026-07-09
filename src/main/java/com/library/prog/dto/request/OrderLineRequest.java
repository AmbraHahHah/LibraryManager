package com.library.prog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderLineRequest(
    UUID orderId,
    @NotNull UUID copyId,
    @NotNull @Positive Integer quantity,
    @PositiveOrZero BigDecimal unitPrice,
    @PositiveOrZero BigDecimal discount) {}
