package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.ClientRequest;
import com.library.prog.dto.response.ClientResponse;
import com.library.prog.service.ClientService;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @GetMapping
  public List<ClientResponse> findAll() {
    return clientService.findAll();
  }

  @GetMapping("/{id}")
  public ClientResponse findById(@PathVariable UUID id) {
    return clientService.findById(id);
  }

  @PostMapping
  public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
    var response = clientService.create(request);
    return ResponseEntity.created(URI.create("/clients/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
    return clientService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    clientService.delete(id);
  }
}
