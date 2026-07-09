package com.library.prog.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CategoryResponse(
    UUID id,
    String name,
    String description,
    UUID parentId,
    String parentName,
    List<CategoryChildResponse> subCategories) {}
