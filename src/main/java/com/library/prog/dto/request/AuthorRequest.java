package com.library.prog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record AuthorRequest(
    @NotBlank @Size(max = 100) String lastName,
    @Size(max = 100) String firstName,
    String biography,
    @Size(max = 100) String nationality,
    LocalDate birthDate) {}
