package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.request.CopyRequest;
import com.library.prog.dto.request.EditorRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.dto.response.EditorResponse;
import com.library.prog.model.FormatEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class CopyControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  private UUID bookId;
  private UUID publisherId;

  @BeforeEach
  void setUp() {
    var bookReq = BookRequest.builder().title("Le Petit Prince").build();
    var bookRes = rest.postForEntity("/books", bookReq, BookResponse.class).getBody();
    bookId = bookRes.id();

    var editorReq =
        EditorRequest.builder()
            .name("Gallimard")
            .email("contact@gallimard.fr")
            .country("France")
            .build();
    var editorRes = rest.postForEntity("/editors", editorReq, EditorResponse.class).getBody();
    publisherId = editorRes.id();
  }

  @Test
  void create_and_find_copy() {
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .price(new BigDecimal("12.99"))
            .pageCount(200)
            .publicationDate(LocalDate.of(2020, 1, 1))
            .bookId(bookId)
            .publisherId(publisherId)
            .build();

    var created = rest.postForEntity("/copies", request, CopyResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("9782070612758", created.getBody().isbn());
    assertEquals(FormatEnum.PAPERBACK, created.getBody().format());
    assertEquals(bookId, created.getBody().bookId());
    assertEquals(publisherId, created.getBody().publisherId());

    var found = rest.getForEntity("/copies/" + created.getBody().id(), CopyResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("9782070612758", found.getBody().isbn());
  }

  @Test
  void list_all_copies() {
    rest.postForEntity(
        "/copies",
        CopyRequest.builder()
            .isbn("1111111111")
            .format(FormatEnum.PAPERBACK)
            .bookId(bookId)
            .build(),
        CopyResponse.class);
    rest.postForEntity(
        "/copies",
        CopyRequest.builder().isbn("2222222222").format(FormatEnum.EBOOK).bookId(bookId).build(),
        CopyResponse.class);

    var response = rest.getForEntity("/copies", CopyResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void update_copy() {
    var created =
        rest.postForEntity(
            "/copies",
            CopyRequest.builder()
                .isbn("9782070612758")
                .format(FormatEnum.PAPERBACK)
                .bookId(bookId)
                .build(),
            CopyResponse.class);
    var updateRequest =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.HARDCOVER)
            .price(new BigDecimal("19.99"))
            .pageCount(250)
            .bookId(bookId)
            .publisherId(publisherId)
            .build();

    rest.put("/copies/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/copies/" + created.getBody().id(), CopyResponse.class);

    assertEquals(FormatEnum.HARDCOVER, updated.getBody().format());
    assertEquals(0, new BigDecimal("19.99").compareTo(updated.getBody().price()));
    assertEquals(250, updated.getBody().pageCount());
    assertEquals(publisherId, updated.getBody().publisherId());
  }

  @Test
  void delete_copy() {
    var created =
        rest.postForEntity(
            "/copies",
            CopyRequest.builder()
                .isbn("9782070612758")
                .format(FormatEnum.PAPERBACK)
                .bookId(bookId)
                .build(),
            CopyResponse.class);
    var id = created.getBody().id();

    rest.delete("/copies/" + id);

    var found = rest.getForEntity("/copies/" + id, CopyResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_isbn_is_blank() {
    var request =
        CopyRequest.builder().isbn("").format(FormatEnum.PAPERBACK).bookId(bookId).build();

    var response = rest.postForEntity("/copies", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_400_when_format_is_null() {
    var request = CopyRequest.builder().isbn("9782070612758").bookId(bookId).build();

    var response = rest.postForEntity("/copies", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_400_when_bookId_is_null() {
    var request = CopyRequest.builder().isbn("9782070612758").format(FormatEnum.PAPERBACK).build();

    var response = rest.postForEntity("/copies", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_copy_not_found() {
    var response = rest.getForEntity("/copies/" + UUID.randomUUID(), CopyResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
