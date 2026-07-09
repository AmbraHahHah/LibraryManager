package com.library.prog.service;

import com.library.prog.dto.request.ClientRequest;
import com.library.prog.dto.response.ClientResponse;
import com.library.prog.model.Client;
import com.library.prog.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ClientRepository clientRepository;

  @Transactional(readOnly = true)
  public List<ClientResponse> findAll() {
    return clientRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ClientResponse findById(UUID id) {
    return clientRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));
  }

  @Transactional
  public ClientResponse create(ClientRequest request) {
    var client =
        Client.builder()
            .lastName(request.lastName())
            .firstName(request.firstName())
            .email(request.email())
            .phone(request.phone())
            .address(request.address())
            .city(request.city())
            .postalCode(request.postalCode())
            .country(request.country())
            .build();
    return toResponse(clientRepository.save(client));
  }

  @Transactional
  public ClientResponse update(UUID id, ClientRequest request) {
    var client =
        clientRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));
    client.setLastName(request.lastName());
    client.setFirstName(request.firstName());
    client.setEmail(request.email());
    client.setPhone(request.phone());
    client.setAddress(request.address());
    client.setCity(request.city());
    client.setPostalCode(request.postalCode());
    client.setCountry(request.country());
    return toResponse(clientRepository.save(client));
  }

  @Transactional
  public void delete(UUID id) {
    if (!clientRepository.existsById(id)) {
      throw new EntityNotFoundException("Client not found: " + id);
    }
    clientRepository.deleteById(id);
  }

  private ClientResponse toResponse(Client client) {
    return ClientResponse.builder()
        .id(client.getId())
        .lastName(client.getLastName())
        .firstName(client.getFirstName())
        .email(client.getEmail())
        .phone(client.getPhone())
        .address(client.getAddress())
        .city(client.getCity())
        .postalCode(client.getPostalCode())
        .country(client.getCountry())
        .active(client.getActive())
        .registrationDate(client.getRegistrationDate())
        .build();
  }
}
