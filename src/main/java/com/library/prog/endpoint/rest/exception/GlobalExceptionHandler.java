package com.library.prog.endpoint.rest.exception;

import com.library.prog.dto.response.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ApiError handleNotFound(EntityNotFoundException ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(getPath(request))
        .build();
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ApiError handleNoResource(NoResourceFoundException ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(getPath(request))
        .build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiError handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    var fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fe ->
                    ApiError.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
            .toList();
    return ApiError.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Validation failed")
        .path(getPath(request))
        .fieldErrors(fieldErrors)
        .build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ApiError handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    var fieldErrors =
        ex.getConstraintViolations().stream()
            .map(
                cv ->
                    ApiError.FieldError.builder()
                        .field(cv.getPropertyPath().toString())
                        .message(cv.getMessage())
                        .build())
            .toList();
    return ApiError.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Constraint violation")
        .path(getPath(request))
        .fieldErrors(fieldErrors)
        .build();
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ApiError handleMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Malformed request body")
        .path(getPath(request))
        .build();
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Invalid value for parameter '" + ex.getName() + "'")
        .path(getPath(request))
        .build();
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ApiError handleConflict(DataIntegrityViolationException ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.CONFLICT.value())
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .message("Data integrity violation")
        .path(getPath(request))
        .build();
  }

  @ExceptionHandler(Exception.class)
  public ApiError handleGeneral(Exception ex, WebRequest request) {
    return ApiError.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("An unexpected error occurred")
        .path(getPath(request))
        .build();
  }

  private String getPath(WebRequest request) {
    var description = request.getDescription(false);
    return description != null && description.startsWith("uri=")
        ? description.substring(4)
        : description;
  }
}
