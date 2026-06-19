package com.library.prog.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ApiError(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    List<FieldError> fieldErrors) {

  public ApiError(int status, String error, String message, String path) {
    this(status, error, message, path, Instant.now(), List.of());
  }

  @Builder
  public record FieldError(String field, String message) {}
}
