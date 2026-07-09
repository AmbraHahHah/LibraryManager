package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.ClientRequest;
import com.library.prog.model.Client;
import com.library.prog.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

  @Mock private ClientRepository clientRepository;

  @InjectMocks private ClientService clientService;

  @Test
  void findAll_returns_all_clients() {
    var client = buildClient("Doe");
    when(clientRepository.findAll()).thenReturn(List.of(client));

    var result = clientService.findAll();

    assertEquals(1, result.size());
    assertEquals("Doe", result.getFirst().lastName());
  }

  @Test
  void findById_returns_client_when_found() {
    var client = buildClient("Doe");
    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

    var result = clientService.findById(client.getId());

    assertEquals("Doe", result.lastName());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(clientRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> clientService.findById(id));
  }

  @Test
  void create_saves_and_returns_client() {
    var request =
        ClientRequest.builder()
            .lastName("Doe")
            .firstName("John")
            .email("john@test.com")
            .phone("+33123456789")
            .address("1 rue Test")
            .city("Paris")
            .postalCode("75001")
            .country("France")
            .build();
    var savedClient = buildClient("Doe");
    savedClient.setFirstName("John");
    savedClient.setEmail("john@test.com");
    savedClient.setPhone("+33123456789");
    savedClient.setAddress("1 rue Test");
    savedClient.setCity("Paris");
    savedClient.setPostalCode("75001");
    savedClient.setCountry("France");
    when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

    var result = clientService.create(request);

    assertEquals("Doe", result.lastName());
    assertEquals("John", result.firstName());
    assertEquals("john@test.com", result.email());
    assertEquals("+33123456789", result.phone());
    assertEquals("1 rue Test", result.address());
    assertEquals("Paris", result.city());
    assertEquals("75001", result.postalCode());
    assertEquals("France", result.country());
    assertNotNull(result.id());
  }

  @Test
  void create_with_minimal_fields() {
    var request =
        ClientRequest.builder()
            .lastName("Minimal")
            .firstName("User")
            .email("minimal@test.com")
            .build();
    var captor = ArgumentCaptor.forClass(Client.class);
    when(clientRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    clientService.create(request);

    assertEquals("Minimal", captor.getValue().getLastName());
    assertEquals("User", captor.getValue().getFirstName());
    assertEquals("minimal@test.com", captor.getValue().getEmail());
    assertNull(captor.getValue().getPhone());
    assertTrue(captor.getValue().getActive());
  }

  @Test
  void update_modifies_existing_client() {
    var existing = buildClient("Old");
    var request =
        ClientRequest.builder().lastName("Updated").firstName("New").email("new@test.com").build();
    when(clientRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(clientRepository.save(any(Client.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = clientService.update(existing.getId(), request);

    assertEquals("Updated", result.lastName());
    assertEquals("New", result.firstName());
    assertEquals("new@test.com", result.email());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = ClientRequest.builder().lastName("Any").firstName("Any").email("a@b.com").build();
    when(clientRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> clientService.update(id, request));
  }

  @Test
  void delete_removes_client_when_exists() {
    var id = UUID.randomUUID();
    when(clientRepository.existsById(id)).thenReturn(true);

    clientService.delete(id);

    verify(clientRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(clientRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> clientService.delete(id));
  }

  private Client buildClient(String lastName) {
    return Client.builder().id(UUID.randomUUID()).lastName(lastName).build();
  }
}
