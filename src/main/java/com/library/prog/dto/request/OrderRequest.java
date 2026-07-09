package com.library.prog.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrderRequest(
    @NotNull UUID clientId,
    String shippingAddress,
    String paymentMethod,
    @NotNull @Valid @Size(min = 1) List<OrderLineRequest> lines) {}
