package com.library.prog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderLineRequest(@NotNull UUID copyId, @NotNull @Positive Integer quantity) {}
