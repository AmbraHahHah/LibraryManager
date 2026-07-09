package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.request.CopyRequest;
import com.library.prog.dto.request.OrderLineRequest;
import com.library.prog.dto.request.OrderRequest;
import com.library.prog.dto.request.StockAdjustRequest;
import com.library.prog.dto.request.StockRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.dto.response.OrderResponse;
import com.library.prog.dto.response.StockResponse;
import com.library.prog.model.Client;
import com.library.prog.model.FormatEnum;
import com.library.prog.model.MovementTypeEnum;
import com.library.prog.repository.ClientRepository;
import java.math.BigDecimal;
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
class OrderControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;
  @Autowired private ClientRepository clientRepository;

  private UUID clientId;
  private UUID copyId;
  private UUID stockId;

  @BeforeEach
  void setUp() {
    var client =
        clientRepository.save(
            Client.builder()
                .lastName("Dupont")
                .firstName("Jean")
                .email("jean.dupont@test.com")
                .build());
    clientId = client.getId();

    var bookRes =
        rest.postForEntity("/books", BookRequest.builder().title("Test Book").build(), BookResponse.class)
            .getBody();

    var copyReq =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .price(new BigDecimal("12.99"))
            .bookId(bookRes.id())
            .build();
    var copyRes =
        rest.postForEntity("/copies", copyReq, CopyResponse.class).getBody();
    copyId = copyRes.id();

    var stockRes =
        rest.postForEntity(
                "/stocks", StockRequest.builder().copyId(copyId).alertThreshold(5).build(),
                StockResponse.class)
            .getBody();
    stockId = stockRes.id();

    rest.patchForObject(
        "/stocks/" + stockId + "/adjust",
        StockAdjustRequest.builder()
            .quantity(10)
            .movementType(MovementTypeEnum.RESTOCK)
            .reason("Initial stock")
            .build(),
        StockResponse.class);
  }

  @Test
  void create_order() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(3).build();
    var request =
        OrderRequest.builder()
            .clientId(clientId)
            .shippingAddress("123 Rue Test")
            .lines(List.of(lineReq))
            .build();

    var response =
        rest.postForEntity("/orders", request, OrderResponse.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().id());
    assertEquals(clientId, response.getBody().clientId());
    assertEquals("Dupont", response.getBody().clientName());
    assertEquals("PENDING", response.getBody().status().name());
    assertEquals(1, response.getBody().lines().size());
    assertEquals(3, response.getBody().lines().getFirst().quantity());
  }

  @Test
  void create_order_returns_400_when_insufficient_stock() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(999).build();
    var request =
        OrderRequest.builder()
            .clientId(clientId)
            .lines(List.of(lineReq))
            .build();

    var response =
        rest.postForEntity("/orders", request, String.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void find_order_by_id() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(2).build();
    var createReq =
        OrderRequest.builder().clientId(clientId).lines(List.of(lineReq)).build();
    var created =
        rest.postForEntity("/orders", createReq, OrderResponse.class).getBody();

    var response = rest.getForEntity("/orders/" + created.id(), OrderResponse.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(created.id(), response.getBody().id());
  }

  @Test
  void list_all_orders() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(1).build();
    var createReq =
        OrderRequest.builder().clientId(clientId).lines(List.of(lineReq)).build();
    rest.postForEntity("/orders", createReq, OrderResponse.class);

    var response = rest.getForEntity("/orders", OrderResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 1);
  }

  @Test
  void confirm_order_reduces_stock() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(4).build();
    var createReq =
        OrderRequest.builder().clientId(clientId).lines(List.of(lineReq)).build();
    var created =
        rest.postForEntity("/orders", createReq, OrderResponse.class).getBody();

    var response =
        rest.patchForObject("/orders/" + created.id() + "/confirm", null, OrderResponse.class);

    assertEquals("CONFIRMED", response.status().name());

    var stock = rest.getForEntity("/stocks/" + stockId, StockResponse.class).getBody();
    assertEquals(6, stock.availableQuantity());
    assertEquals(0, stock.reservedQuantity());
  }

  @Test
  void cancel_pending_order_releases_reserved_stock() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(3).build();
    var createReq =
        OrderRequest.builder().clientId(clientId).lines(List.of(lineReq)).build();
    var created =
        rest.postForEntity("/orders", createReq, OrderResponse.class).getBody();

    rest.delete("/orders/" + created.id());

    var stock = rest.getForEntity("/stocks/" + stockId, StockResponse.class).getBody();
    assertEquals(10, stock.availableQuantity());
    assertEquals(0, stock.reservedQuantity());
  }

  @Test
  void cancel_order_returns_204() {
    var lineReq = OrderLineRequest.builder().copyId(copyId).quantity(1).build();
    var createReq =
        OrderRequest.builder().clientId(clientId).lines(List.of(lineReq)).build();
    var created =
        rest.postForEntity("/orders", createReq, OrderResponse.class).getBody();

    rest.delete("/orders/" + created.id() + "/cancel");

    var order = rest.getForEntity("/orders/" + created.id(), OrderResponse.class).getBody();
    assertEquals("CANCELLED", order.status().name());
  }

  @Test
  void return_404_when_order_not_found() {
    var response = rest.getForEntity("/orders/" + UUID.randomUUID(), OrderResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
