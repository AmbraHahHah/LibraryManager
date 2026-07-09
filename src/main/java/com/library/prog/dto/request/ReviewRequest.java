package com.library.prog.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ReviewRequest(
    @NotNull UUID bookId,
    @NotNull UUID clientId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(max = 2000) String comment) {}
