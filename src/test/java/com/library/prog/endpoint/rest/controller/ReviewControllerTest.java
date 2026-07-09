package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.request.ClientRequest;
import com.library.prog.dto.request.ReviewRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.ClientResponse;
import com.library.prog.dto.response.ReviewResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class ReviewControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  private UUID bookId;
  private UUID clientId;

  @BeforeEach
  void setUp() {
    var book =
        rest.postForEntity(
            "/books", BookRequest.builder().title("Test Book").build(), BookResponse.class);
    bookId = book.getBody().id();

    var client =
        rest.postForEntity(
            "/clients",
            ClientRequest.builder()
                .lastName("Doe")
                .firstName("John")
                .email("john.review@test.com")
                .build(),
            ClientResponse.class);
    clientId = client.getBody().id();
  }

  @Test
  void create_and_find_review() {
    var request =
        ReviewRequest.builder()
            .bookId(bookId)
            .clientId(clientId)
            .rating(4)
            .comment("Good book")
            .build();

    var created = rest.postForEntity("/reviews", request, ReviewResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals(bookId, created.getBody().bookId());
    assertEquals(clientId, created.getBody().clientId());
    assertEquals(4, created.getBody().rating());
    assertEquals("Good book", created.getBody().comment());
    assertFalse(created.getBody().valid());

    var found = rest.getForEntity("/reviews/" + created.getBody().id(), ReviewResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals(4, found.getBody().rating());
  }

  @Test
  void list_all_reviews() {
    rest.postForEntity(
        "/reviews",
        ReviewRequest.builder().bookId(bookId).clientId(clientId).rating(4).build(),
        ReviewResponse.class);

    var response = rest.getForEntity("/reviews", ReviewResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 1);
  }

  @Test
  void update_review() {
    var created =
        rest.postForEntity(
            "/reviews",
            ReviewRequest.builder()
                .bookId(bookId)
                .clientId(clientId)
                .rating(3)
                .comment("Okay")
                .build(),
            ReviewResponse.class);
    var updateRequest =
        ReviewRequest.builder()
            .bookId(bookId)
            .clientId(clientId)
            .rating(5)
            .comment("Excellent!")
            .build();

    rest.put("/reviews/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/reviews/" + created.getBody().id(), ReviewResponse.class);

    assertEquals(5, updated.getBody().rating());
    assertEquals("Excellent!", updated.getBody().comment());
  }

  @Test
  void delete_review() {
    var created =
        rest.postForEntity(
            "/reviews",
            ReviewRequest.builder().bookId(bookId).clientId(clientId).rating(4).build(),
            ReviewResponse.class);
    var id = created.getBody().id();

    rest.delete("/reviews/" + id);

    var found = rest.getForEntity("/reviews/" + id, ReviewResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_rating_is_invalid() {
    var request = ReviewRequest.builder().bookId(bookId).clientId(clientId).rating(6).build();

    var response = rest.postForEntity("/reviews", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_400_when_bookId_is_null() {
    var request = ReviewRequest.builder().clientId(clientId).rating(4).build();

    var response = rest.postForEntity("/reviews", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_review_not_found() {
    var response = rest.getForEntity("/reviews/" + UUID.randomUUID(), ReviewResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
