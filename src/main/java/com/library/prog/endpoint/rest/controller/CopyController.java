package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.CopyRequest;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.service.CopyService;
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
@RequestMapping("/copies")
@RequiredArgsConstructor
public class CopyController {

  private final CopyService copyService;

  @GetMapping
  public List<CopyResponse> findAll() {
    return copyService.findAll();
  }

  @GetMapping("/{id}")
  public CopyResponse findById(@PathVariable UUID id) {
    return copyService.findById(id);
  }

  @PostMapping
  public ResponseEntity<CopyResponse> create(@Valid @RequestBody CopyRequest request) {
    var response = copyService.create(request);
    return ResponseEntity.created(URI.create("/copies/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public CopyResponse update(@PathVariable UUID id, @Valid @RequestBody CopyRequest request) {
    return copyService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    copyService.delete(id);
  }
}
