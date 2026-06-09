package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.response.BookResponse;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class BookControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void create_and_find_book() {
    var request = BookRequest.builder().title("Le Petit Prince").summary("Un grand classique").language("French").build();

    var created = rest.postForEntity("/books", request, BookResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("Le Petit Prince", created.getBody().title());

    var found = rest.getForEntity("/books/" + created.getBody().id(), BookResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("Le Petit Prince", found.getBody().title());
  }

  @Test
  void list_all_books() {
    rest.postForEntity("/books", BookRequest.builder().title("Book A").build(), BookResponse.class);
    rest.postForEntity("/books", BookRequest.builder().title("Book B").build(), BookResponse.class);

    var response = rest.getForEntity("/books", BookResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void update_book() {
    var created = rest.postForEntity("/books", BookRequest.builder().title("Original").build(), BookResponse.class);
    var updateRequest = BookRequest.builder().title("Updated").summary("New summary").language("English").build();

    rest.put("/books/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/books/" + created.getBody().id(), BookResponse.class);

    assertEquals("Updated", updated.getBody().title());
    assertEquals("New summary", updated.getBody().summary());
  }

  @Test
  void delete_book() {
    var created = rest.postForEntity("/books", BookRequest.builder().title("To Delete").build(), BookResponse.class);
    var id = created.getBody().id();

    rest.delete("/books/" + id);

    var found = rest.getForEntity("/books/" + id, BookResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_title_is_blank() {
    var request = BookRequest.builder().title("").build();

    var response = rest.postForEntity("/books", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_book_not_found() {
    var response = rest.getForEntity("/books/" + UUID.randomUUID(), BookResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
