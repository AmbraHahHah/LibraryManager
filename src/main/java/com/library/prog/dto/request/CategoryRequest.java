package com.library.prog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CategoryRequest(
    @NotBlank @Size(max = 100) String name, String description, UUID parentId) {}
