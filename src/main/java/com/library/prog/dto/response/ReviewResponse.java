package com.library.prog.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ReviewResponse(
    UUID id,
    UUID bookId,
    UUID clientId,
    Integer rating,
    String comment,
    Boolean valid,
    Instant createdAt) {}
