package com.library.prog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EditorRequest(
	@NotBlank @Size(max = 150) String name,
	String address,
	@Size(max = 255) String email,
	@Size(max = 100) String country
) {}
