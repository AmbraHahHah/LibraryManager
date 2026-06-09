package com.library.prog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BookRequest(
    @NotBlank @Size(max = 500) String title, String summary, @Size(max = 50) String language) {}
