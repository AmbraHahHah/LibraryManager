package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.EditorRequest;
import com.library.prog.dto.response.EditorResponse;
import com.library.prog.service.EditorService;
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
@RequestMapping("/editors")
@RequiredArgsConstructor
public class EditorController {

  private final EditorService editorService;

  @GetMapping
  public List<EditorResponse> findAll() {
    return editorService.findAll();
  }

  @GetMapping("/{id}")
  public EditorResponse findById(@PathVariable UUID id) {
    return editorService.findById(id);
  }

  @PostMapping
  public ResponseEntity<EditorResponse> create(@Valid @RequestBody EditorRequest request) {
    var response = editorService.create(request);
    return ResponseEntity.created(URI.create("/editors/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public EditorResponse update(@PathVariable UUID id, @Valid @RequestBody EditorRequest request) {
    return editorService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    editorService.delete(id);
  }
}
