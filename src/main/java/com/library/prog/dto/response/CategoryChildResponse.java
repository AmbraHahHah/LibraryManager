package com.library.prog.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CategoryChildResponse(UUID id, String name) {}
