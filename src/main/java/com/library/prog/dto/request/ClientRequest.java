package com.library.prog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientRequest(
    @NotBlank @Size(max = 200) String lastName,
    @NotBlank @Size(max = 200) String firstName,
    @NotBlank @Email @Size(max = 250) String email,
    @Size(max = 50) String phone,
    String address,
    @Size(max = 150) String city,
    @Size(max = 20) String postalCode,
    @Size(max = 100) String country) {}
