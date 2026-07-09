package com.library.prog.endpoint.rest.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Mock private WebRequest request;

  @Test
  void handleEntityNotFound() {
    when(request.getDescription(false)).thenReturn("uri=/books/123");
    var ex = new EntityNotFoundException("Book not found: 123");

    var result = handler.handleNotFound(ex, request);

    assertEquals(404, result.status());
    assertEquals("Not Found", result.error());
    assertEquals("Book not found: 123", result.message());
    assertEquals("/books/123", result.path());
  }

  @Test
  void handleEntityNotFound_with_null_description() {
    when(request.getDescription(false)).thenReturn(null);
    var ex = new EntityNotFoundException("Not found");

    var result = handler.handleNotFound(ex, request);

    assertNull(result.path());
  }

  @Test
  void handleNoResourceFound() {
    when(request.getDescription(false)).thenReturn("uri=/unknown");
    var ex = new NoResourceFoundException(null, "No static resource");

    var result = handler.handleNoResource(ex, request);

    assertEquals(404, result.status());
    assertEquals("Not Found", result.error());
    assertEquals("/unknown", result.path());
  }

  @Test
  void handleValidation() {
    when(request.getDescription(false)).thenReturn("uri=/books");
    var fieldError = new FieldError("object", "title", "Title is required");
    var bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
    var ex = new MethodArgumentNotValidException(null, bindingResult);

    var result = handler.handleValidation(ex, request);

    assertEquals(400, result.status());
    assertEquals("Bad Request", result.error());
    assertEquals("Validation failed", result.message());
    assertEquals("/books", result.path());
    assertEquals(1, result.fieldErrors().size());
    assertEquals("title", result.fieldErrors().getFirst().field());
    assertEquals("Title is required", result.fieldErrors().getFirst().message());
  }

  @Test
  void handleConstraintViolation() {
    when(request.getDescription(false)).thenReturn("uri=/stocks");
    var path = mock(Path.class);
    when(path.toString()).thenReturn("quantity");
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must be positive");
    var ex = new ConstraintViolationException(Set.of(violation));

    var result = handler.handleConstraintViolation(ex, request);

    assertEquals(400, result.status());
    assertEquals("Bad Request", result.error());
    assertEquals("Constraint violation", result.message());
    assertEquals(1, result.fieldErrors().size());
    assertEquals("quantity", result.fieldErrors().getFirst().field());
    assertEquals("must be positive", result.fieldErrors().getFirst().message());
  }

  @Test
  void handleMessageNotReadable() {
    when(request.getDescription(false)).thenReturn("uri=/books");
    var ex = new HttpMessageNotReadableException("Malformed JSON");

    var result = handler.handleMessageNotReadable(ex, request);

    assertEquals(400, result.status());
    assertEquals("Bad Request", result.error());
    assertEquals("Malformed request body", result.message());
    assertEquals("/books", result.path());
  }

  @Test
  void handleTypeMismatch() {
    when(request.getDescription(false)).thenReturn("uri=/books/abc");
    var ex = new MethodArgumentTypeMismatchException("abc", null, null, null, null);

    var result = handler.handleTypeMismatch(ex, request);

    assertEquals(400, result.status());
    assertEquals("Bad Request", result.error());
    assertTrue(result.message().contains("Invalid value"));
  }

  @Test
  void handleDataIntegrityViolation() {
    when(request.getDescription(false)).thenReturn("uri=/books");
    var ex = new DataIntegrityViolationException("Constraint violation");

    var result = handler.handleConflict(ex, request);

    assertEquals(409, result.status());
    assertEquals("Conflict", result.error());
    assertEquals("Data integrity violation", result.message());
    assertEquals("/books", result.path());
  }

  @Test
  void handleGeneralException() {
    when(request.getDescription(false)).thenReturn("uri=/test");
    var ex = new RuntimeException("Something went wrong");

    var result = handler.handleGeneral(ex, request);

    assertEquals(500, result.status());
    assertEquals("Internal Server Error", result.error());
    assertEquals("An unexpected error occurred", result.message());
    assertEquals("/test", result.path());
  }

  @Test
  void getPath_without_uri_prefix() {
    when(request.getDescription(false)).thenReturn("description without uri=");
    var ex = new EntityNotFoundException("Not found");

    var result = handler.handleNotFound(ex, request);

    assertEquals("description without uri=", result.path());
  }
}
