package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.CategoryRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.CategoryResponse;
import com.library.prog.service.BookService;
import com.library.prog.service.CategoryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;
  private final BookService bookService;

  @GetMapping
  public List<CategoryResponse> findAll() {
    return categoryService.findAll();
  }

  @GetMapping("/roots")
  public List<CategoryResponse> findRootCategories() {
    return categoryService.findRootCategories();
  }

  @GetMapping("/{id}")
  public CategoryResponse findById(@PathVariable UUID id) {
    return categoryService.findById(id);
  }

  @GetMapping("/{id}/books")
  public List<BookResponse> getBooks(@PathVariable UUID id) {
    return bookService.findByCategoryId(id);
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    var response = categoryService.create(request);
    return ResponseEntity.created(URI.create("/categories/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public CategoryResponse update(
      @PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
    return categoryService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    categoryService.delete(id);
  }
}
