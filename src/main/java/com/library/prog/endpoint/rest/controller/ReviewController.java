package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.ReviewRequest;
import com.library.prog.dto.response.ReviewResponse;
import com.library.prog.service.ReviewService;
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
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @GetMapping
  public List<ReviewResponse> findAll() {
    return reviewService.findAll();
  }

  @GetMapping("/{id}")
  public ReviewResponse findById(@PathVariable UUID id) {
    return reviewService.findById(id);
  }

  @PostMapping
  public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
    var response = reviewService.create(request);
    return ResponseEntity.created(URI.create("/reviews/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public ReviewResponse update(@PathVariable UUID id, @Valid @RequestBody ReviewRequest request) {
    return reviewService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    reviewService.delete(id);
  }
}
