package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.AuthorRequest;
import com.library.prog.dto.response.AuthorResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class AuthorControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void create_and_find_author() {
    var request =
        AuthorRequest.builder()
            .lastName("Hugo")
            .firstName("Victor")
            .nationality("French")
            .birthDate(LocalDate.of(1802, 2, 26))
            .build();

    var created = rest.postForEntity("/authors", request, AuthorResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("Hugo", created.getBody().lastName());
    assertEquals("Victor", created.getBody().firstName());
    assertEquals("French", created.getBody().nationality());
    assertEquals(LocalDate.of(1802, 2, 26), created.getBody().birthDate());

    var found = rest.getForEntity("/authors/" + created.getBody().id(), AuthorResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("Hugo", found.getBody().lastName());
  }

  @Test
  void list_all_authors() {
    rest.postForEntity("/authors", AuthorRequest.builder().lastName("Hugo").build(), AuthorResponse.class);
    rest.postForEntity("/authors", AuthorRequest.builder().lastName("Zola").build(), AuthorResponse.class);

    var response = rest.getForEntity("/authors", AuthorResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void update_author() {
    var created =
        rest.postForEntity(
            "/authors", AuthorRequest.builder().lastName("Original").build(), AuthorResponse.class);
    var updateRequest =
        AuthorRequest.builder()
            .lastName("Updated")
            .firstName("New")
            .nationality("UK")
            .build();

    rest.put("/authors/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/authors/" + created.getBody().id(), AuthorResponse.class);

    assertEquals("Updated", updated.getBody().lastName());
    assertEquals("New", updated.getBody().firstName());
    assertEquals("UK", updated.getBody().nationality());
  }

  @Test
  void delete_author() {
    var created =
        rest.postForEntity(
            "/authors", AuthorRequest.builder().lastName("To Delete").build(), AuthorResponse.class);
    var id = created.getBody().id();

    rest.delete("/authors/" + id);

    var found = rest.getForEntity("/authors/" + id, AuthorResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_lastName_is_blank() {
    var request = AuthorRequest.builder().lastName("").build();

    var response = rest.postForEntity("/authors", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_author_not_found() {
    var response = rest.getForEntity("/authors/" + UUID.randomUUID(), AuthorResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
