package com.library.prog.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BookResponse(
    UUID id, String title, String summary, String language, Instant createdAt) {}
