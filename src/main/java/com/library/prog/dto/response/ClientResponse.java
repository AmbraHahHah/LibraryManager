package com.library.prog.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClientResponse(
    UUID id,
    String lastName,
    String firstName,
    String email,
    String phone,
    String address,
    String city,
    String postalCode,
    String country,
    Boolean active,
    Instant registrationDate) {}
