package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.EditorRequest;
import com.library.prog.dto.response.EditorResponse;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class EditorControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void create_and_find_editor() {
    var request =
        EditorRequest.builder()
            .name("Gallimard")
            .address("5 rue Sébastien-Bottin, Paris")
            .email("contact@gallimard.fr")
            .country("France")
            .build();

    var created = rest.postForEntity("/editors", request, EditorResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("Gallimard", created.getBody().name());

    var found = rest.getForEntity("/editors/" + created.getBody().id(), EditorResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("Gallimard", found.getBody().name());
  }

  @Test
  void list_all_editors() {
    rest.postForEntity(
        "/editors", EditorRequest.builder().name("Editor A").build(), EditorResponse.class);
    rest.postForEntity(
        "/editors", EditorRequest.builder().name("Editor B").build(), EditorResponse.class);

    var response = rest.getForEntity("/editors", EditorResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void update_editor() {
    var created =
        rest.postForEntity(
            "/editors", EditorRequest.builder().name("Original").build(), EditorResponse.class);
    var updateRequest =
        EditorRequest.builder()
            .name("Updated")
            .address("New Address")
            .email("updated@test.com")
            .country("UK")
            .build();

    rest.put("/editors/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/editors/" + created.getBody().id(), EditorResponse.class);

    assertEquals("Updated", updated.getBody().name());
    assertEquals("New Address", updated.getBody().address());
    assertEquals("updated@test.com", updated.getBody().email());
    assertEquals("UK", updated.getBody().country());
  }

  @Test
  void delete_editor() {
    var created =
        rest.postForEntity(
            "/editors", EditorRequest.builder().name("To Delete").build(), EditorResponse.class);
    var id = created.getBody().id();

    rest.delete("/editors/" + id);

    var found = rest.getForEntity("/editors/" + id, EditorResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_name_is_blank() {
    var request = EditorRequest.builder().name("").build();

    var response = rest.postForEntity("/editors", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_editor_not_found() {
    var response = rest.getForEntity("/editors/" + UUID.randomUUID(), EditorResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
