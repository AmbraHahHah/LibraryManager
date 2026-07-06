package com.library.prog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StockRequest(@NotNull UUID copyId, @PositiveOrZero Integer alertThreshold) {}
