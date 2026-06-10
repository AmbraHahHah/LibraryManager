package com.library.prog.dto.response;

import com.library.prog.model.FormatEnum;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CopyResponse(
    UUID id,
    String isbn,
    FormatEnum format,
    BigDecimal price,
    Integer pageCount,
    LocalDate publicationDate,
    String imageUrl,
    Instant updatedAt,
    UUID bookId,
    UUID publisherId) {}
