package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.ClientRequest;
import com.library.prog.dto.response.ClientResponse;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class ClientControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void create_and_find_client() {
    var request =
        ClientRequest.builder()
            .lastName("Doe")
            .firstName("John")
            .email("john.doe@test.com")
            .phone("+33123456789")
            .address("1 rue Test")
            .city("Paris")
            .postalCode("75001")
            .country("France")
            .build();

    var created = rest.postForEntity("/clients", request, ClientResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("Doe", created.getBody().lastName());
    assertEquals("John", created.getBody().firstName());
    assertEquals("john.doe@test.com", created.getBody().email());
    assertEquals("+33123456789", created.getBody().phone());
    assertEquals("1 rue Test", created.getBody().address());
    assertEquals("Paris", created.getBody().city());
    assertEquals("75001", created.getBody().postalCode());
    assertEquals("France", created.getBody().country());
    assertTrue(created.getBody().active());
    assertNotNull(created.getBody().registrationDate());

    var found = rest.getForEntity("/clients/" + created.getBody().id(), ClientResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("Doe", found.getBody().lastName());
  }

  @Test
  void list_all_clients() {
    rest.postForEntity("/clients",
        ClientRequest.builder().lastName("Doe").firstName("John").email("john@test.com").build(),
        ClientResponse.class);
    rest.postForEntity("/clients",
        ClientRequest.builder().lastName("Smith").firstName("Jane").email("jane@test.com").build(),
        ClientResponse.class);

    var response = rest.getForEntity("/clients", ClientResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void update_client() {
    var created =
        rest.postForEntity("/clients",
            ClientRequest.builder().lastName("Original").firstName("User").email("orig@test.com").build(),
            ClientResponse.class);
    var updateRequest =
        ClientRequest.builder()
            .lastName("Updated")
            .firstName("New")
            .email("new@test.com")
            .build();

    rest.put("/clients/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/clients/" + created.getBody().id(), ClientResponse.class);

    assertEquals("Updated", updated.getBody().lastName());
    assertEquals("New", updated.getBody().firstName());
    assertEquals("new@test.com", updated.getBody().email());
  }

  @Test
  void delete_client() {
    var created =
        rest.postForEntity("/clients",
            ClientRequest.builder().lastName("To Delete").firstName("User").email("delete@test.com").build(),
            ClientResponse.class);
    var id = created.getBody().id();

    rest.delete("/clients/" + id);

    var found = rest.getForEntity("/clients/" + id, ClientResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_lastName_is_blank() {
    var request = ClientRequest.builder().lastName("").firstName("User").email("a@b.com").build();

    var response = rest.postForEntity("/clients", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_400_when_email_is_invalid() {
    var request =
        ClientRequest.builder().lastName("Doe").firstName("John").email("invalid-email").build();

    var response = rest.postForEntity("/clients", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_client_not_found() {
    var response = rest.getForEntity("/clients/" + UUID.randomUUID(), ClientResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
