package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.request.CopyRequest;
import com.library.prog.dto.request.StockAdjustRequest;
import com.library.prog.dto.request.StockRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.dto.response.StockResponse;
import com.library.prog.model.FormatEnum;
import com.library.prog.model.MovementTypeEnum;
import java.util.Arrays;
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
class StockControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  private UUID bookId;

  @BeforeEach
  void setUp() {
    var bookReq = BookRequest.builder().title("Le Petit Prince").build();
    var bookRes = rest.postForEntity("/books", bookReq, BookResponse.class).getBody();
    bookId = bookRes.id();
  }

  private UUID createCopy(String isbn) {
    var copyReq =
        CopyRequest.builder().isbn(isbn).format(FormatEnum.PAPERBACK).bookId(bookId).build();
    return rest.postForEntity("/copies", copyReq, CopyResponse.class).getBody().id();
  }

  private StockResponse createStock(String isbn) {
    var copyId = createCopy(isbn);
    var stockReq = StockRequest.builder().copyId(copyId).build();
    return rest.postForEntity("/stocks", stockReq, StockResponse.class).getBody();
  }

  private void restock(UUID stockId, int quantity) {
    var adjustReq =
        StockAdjustRequest.builder()
            .quantity(quantity)
            .movementType(MovementTypeEnum.RESTOCK)
            .build();
    rest.patchForObject("/stocks/" + stockId + "/adjust", adjustReq, StockResponse.class);
  }

  @Test
  void low_stock_uses_default_threshold_of_3() {
    var low = createStock("1111111111");
    restock(low.id(), 2);
    var high = createStock("2222222222");
    restock(high.id(), 20);

    var response = rest.getForEntity("/stocks/low-stock", StockResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var ids = Arrays.stream(response.getBody()).map(StockResponse::id).toList();
    assertTrue(ids.contains(low.id()));
    assertFalse(ids.contains(high.id()));
  }

  @Test
  void low_stock_accepts_custom_threshold() {
    var stock = createStock("3333333333");
    restock(stock.id(), 8);

    var responseDefault = rest.getForEntity("/stocks/low-stock", StockResponse[].class);
    var responseCustom =
        rest.getForEntity("/stocks/low-stock?threshold=10", StockResponse[].class);

    var defaultIds = Arrays.stream(responseDefault.getBody()).map(StockResponse::id).toList();
    var customIds = Arrays.stream(responseCustom.getBody()).map(StockResponse::id).toList();

    assertFalse(defaultIds.contains(stock.id()));
    assertTrue(customIds.contains(stock.id()));
  }

  @Test
  void low_stock_excludes_stock_above_threshold() {
    var stock = createStock("4444444444");
    restock(stock.id(), 100);

    var response = rest.getForEntity("/stocks/low-stock?threshold=3", StockResponse[].class);

    var ids = Arrays.stream(response.getBody()).map(StockResponse::id).toList();
    assertFalse(ids.contains(stock.id()));
  }
}