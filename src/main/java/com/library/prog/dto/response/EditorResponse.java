package com.library.prog.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record EditorResponse(UUID id, String name, String address, String email, String country) {}
