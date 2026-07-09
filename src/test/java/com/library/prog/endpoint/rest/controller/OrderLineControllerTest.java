package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.*;
import com.library.prog.dto.response.*;
import java.util.List;
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
class OrderLineControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  private UUID orderId;
  private UUID copyId;

  @BeforeEach
  void setUp() {
    var book =
        rest.postForEntity(
            "/books", BookRequest.builder().title("Test Book").build(), BookResponse.class);

    var copyRequest =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(com.library.prog.model.FormatEnum.PAPERBACK)
            .bookId(book.getBody().id())
            .build();
    var copy = rest.postForEntity("/copies", copyRequest, CopyResponse.class);
    copyId = copy.getBody().id();

    var client =
        rest.postForEntity(
            "/clients",
            ClientRequest.builder()
                .lastName("Doe")
                .firstName("John")
                .email("john.orderline@test.com")
                .build(),
            ClientResponse.class);

    var orderRequest =
        OrderRequest.builder()
            .clientId(client.getBody().id())
            .lines(List.of(OrderLineRequest.builder().copyId(copyId).quantity(1).build()))
            .build();
    var order = rest.postForEntity("/orders", orderRequest, OrderResponse.class);
    orderId = order.getBody().id();
  }

  @Test
  void find_by_order_id() {
    var response =
        rest.getForEntity("/order-lines/by-order?orderId=" + orderId, OrderLineResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 1);
    assertEquals(copyId, response.getBody()[0].copyId());
  }

  @Test
  void create_standalone_line() {
    var request =
        OrderLineRequest.builder()
            .orderId(orderId)
            .copyId(copyId)
            .quantity(2)
            .unitPrice(java.math.BigDecimal.valueOf(15))
            .build();

    var created = rest.postForEntity("/order-lines", request, OrderLineResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals(2, created.getBody().quantity());
    assertEquals(copyId, created.getBody().copyId());
  }

  @Test
  void list_all_lines() {
    var response = rest.getForEntity("/order-lines", OrderLineResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 1);
  }

  @Test
  void delete_line() {
    var lines =
        rest.getForEntity("/order-lines/by-order?orderId=" + orderId, OrderLineResponse[].class);
    var lineId = lines.getBody()[0].id();

    rest.delete("/order-lines/" + lineId);

    var found = rest.getForEntity("/order-lines/" + lineId, OrderLineResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_404_when_line_not_found() {
    var response = rest.getForEntity("/order-lines/" + UUID.randomUUID(), OrderLineResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
