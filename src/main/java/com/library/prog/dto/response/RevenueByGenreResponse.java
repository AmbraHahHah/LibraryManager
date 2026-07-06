package com.library.prog.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record RevenueByGenreResponse(String genre, BigDecimal revenue) {}
