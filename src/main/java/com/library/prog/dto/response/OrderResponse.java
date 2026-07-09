package com.library.prog.dto.response;

import com.library.prog.model.OrderStatusEnum;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderResponse(
    UUID id,
    UUID clientId,
    String clientName,
    OrderStatusEnum status,
    BigDecimal totalAmount,
    BigDecimal shippingFee,
    String shippingAddress,
    String paymentMethod,
    String paymentReference,
    Instant orderDate,
    List<OrderLineResponse> lines) {}
