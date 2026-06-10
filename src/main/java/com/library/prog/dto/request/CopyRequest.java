package com.library.prog.dto.request;

import com.library.prog.model.FormatEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CopyRequest(
    @NotBlank @Size(max = 20) String isbn,
    @NotNull FormatEnum format,
    @PositiveOrZero BigDecimal price,
    Integer pageCount,
    LocalDate publicationDate,
    String imageUrl,
    @NotNull UUID bookId,
    UUID publisherId) {}
