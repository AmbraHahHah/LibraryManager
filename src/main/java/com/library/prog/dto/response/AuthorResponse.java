package com.library.prog.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AuthorResponse(
    UUID id,
    String lastName,
    String firstName,
    String biography,
    String nationality,
    LocalDate birthDate) {}
