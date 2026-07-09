package com.library.prog.dto.response;

import com.library.prog.model.FormatEnum;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderLineResponse(
    UUID id,
    UUID copyId,
    String isbn,
    FormatEnum format,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal discount) {}
