package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.AuthorRequest;
import com.library.prog.dto.response.AuthorResponse;
import com.library.prog.service.AuthorService;
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
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

  private final AuthorService authorService;

  @GetMapping
  public List<AuthorResponse> findAll() {
    return authorService.findAll();
  }

  @GetMapping("/{id}")
  public AuthorResponse findById(@PathVariable UUID id) {
    return authorService.findById(id);
  }

  @PostMapping
  public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
    var response = authorService.create(request);
    return ResponseEntity.created(URI.create("/authors/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public AuthorResponse update(@PathVariable UUID id, @Valid @RequestBody AuthorRequest request) {
    return authorService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    authorService.delete(id);
  }
}
