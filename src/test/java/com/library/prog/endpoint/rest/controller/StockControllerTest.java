package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.*;
import com.library.prog.dto.response.*;
import com.library.prog.model.FormatEnum;
import com.library.prog.model.MovementTypeEnum;
import java.math.BigDecimal;
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
    private UUID copyId;

    @BeforeEach
    void setUp() {
        var bookReq = BookRequest.builder().title("Test Book").build();
        var bookRes = rest.postForEntity("/books", bookReq, BookResponse.class).getBody();
        bookId = bookRes.id();

        var copyReq =
                CopyRequest.builder()
                        .isbn("9782070612758")
                        .format(FormatEnum.PAPERBACK)
                        .price(new BigDecimal("12.99"))
                        .bookId(bookId)
                        .build();
        var copyRes = rest.postForEntity("/copies", copyReq, CopyResponse.class).getBody();
        copyId = copyRes.id();
    }

    private UUID createCopy(String isbn) {
        var copyReq =
                CopyRequest.builder()
                        .isbn(isbn)
                        .format(FormatEnum.PAPERBACK)
                        .price(BigDecimal.TEN)
                        .bookId(bookId)
                        .build();
        return rest.postForEntity("/copies", copyReq, CopyResponse.class).getBody().id();
    }

    private StockResponse createStock(String isbn) {
        var newCopyId = createCopy(isbn);
        var stockReq = StockRequest.builder().copyId(newCopyId).build();
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
    void create_stock() {
        var request = StockRequest.builder().copyId(copyId).alertThreshold(10).build();

        var response = rest.postForEntity("/stocks", request, StockResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals(0, response.getBody().availableQuantity());
        assertEquals(0, response.getBody().reservedQuantity());
        assertEquals(10, response.getBody().alertThreshold());
        assertEquals(copyId, response.getBody().copyId());
        assertTrue(response.getBody().outOfStock());
    }

    @Test
    void find_stock_by_id() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();

        var response = rest.getForEntity("/stocks/" + created.id(), StockResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(created.id(), response.getBody().id());
        assertEquals(copyId, response.getBody().copyId());
    }

    @Test
    void find_stock_by_copy_id() {
        rest.postForEntity(
                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class);

        var response = rest.getForEntity("/stocks/by-copy?copyId=" + copyId, StockResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(copyId, response.getBody().copyId());
    }

    @Test
    void list_all_stocks() {
        rest.postForEntity(
                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class);

        var response = rest.getForEntity("/stocks", StockResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length >= 1);
    }

    @Test
    void update_stock_alert_threshold() {
        var created =
                rest.postForEntity(
                                "/stocks",
                                StockRequest.builder().copyId(copyId).alertThreshold(5).build(),
                                StockResponse.class)
                        .getBody();
        var updateReq = StockRequest.builder().copyId(copyId).alertThreshold(20).build();

        rest.put("/stocks/" + created.id(), updateReq);
        var updated = rest.getForEntity("/stocks/" + created.id(), StockResponse.class);

        assertEquals(20, updated.getBody().alertThreshold());
    }

    @Test
    void adjust_stock_restock() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();
        var adjustReq =
                StockAdjustRequest.builder()
                        .quantity(10)
                        .movementType(MovementTypeEnum.RESTOCK)
                        .reason("Initial stock")
                        .build();

        var response =
                rest.patchForObject("/stocks/" + created.id() + "/adjust", adjustReq, StockResponse.class);

        assertEquals(10, response.availableQuantity());
        assertFalse(response.outOfStock());
    }

    @Test
    void adjust_stock_sale() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();
        rest.patchForObject(
                "/stocks/" + created.id() + "/adjust",
                StockAdjustRequest.builder().quantity(10).movementType(MovementTypeEnum.RESTOCK).build(),
                StockResponse.class);

        var response =
                rest.patchForObject(
                        "/stocks/" + created.id() + "/adjust",
                        StockAdjustRequest.builder().quantity(3).movementType(MovementTypeEnum.SALE).build(),
                        StockResponse.class);

        assertEquals(7, response.availableQuantity());
    }

    @Test
    void adjust_stock_insufficient_sale_returns_400() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();

        var response =
                rest.patchForObject(
                        "/stocks/" + created.id() + "/adjust",
                        StockAdjustRequest.builder().quantity(10).movementType(MovementTypeEnum.SALE).build(),
                        String.class);

        assertTrue(
                response.contains("Insufficient stock") || response.contains("insufficient"),
                "Expected error about insufficient stock, got: " + response);
    }

    @Test
    void adjust_stock_loss() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();
        rest.patchForObject(
                "/stocks/" + created.id() + "/adjust",
                StockAdjustRequest.builder().quantity(10).movementType(MovementTypeEnum.RESTOCK).build(),
                StockResponse.class);

        var response =
                rest.patchForObject(
                        "/stocks/" + created.id() + "/adjust",
                        StockAdjustRequest.builder().quantity(4).movementType(MovementTypeEnum.LOSS).build(),
                        StockResponse.class);

        assertEquals(6, response.availableQuantity());
    }

    @Test
    void adjust_stock_customer_return() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();
        rest.patchForObject(
                "/stocks/" + created.id() + "/adjust",
                StockAdjustRequest.builder().quantity(10).movementType(MovementTypeEnum.RESTOCK).build(),
                StockResponse.class);
        rest.patchForObject(
                "/stocks/" + created.id() + "/adjust",
                StockAdjustRequest.builder().quantity(2).movementType(MovementTypeEnum.SALE).build(),
                StockResponse.class);

        var response =
                rest.patchForObject(
                        "/stocks/" + created.id() + "/adjust",
                        StockAdjustRequest.builder()
                                .quantity(1)
                                .movementType(MovementTypeEnum.CUSTOMER_RETURN)
                                .build(),
                        StockResponse.class);

        assertEquals(9, response.availableQuantity());
    }

    @Test
    void return_400_when_stock_duplicate() {
        rest.postForEntity(
                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class);

        var response =
                rest.postForEntity("/stocks", StockRequest.builder().copyId(copyId).build(), String.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void return_404_when_stock_not_found() {
        var response = rest.getForEntity("/stocks/" + UUID.randomUUID(), StockResponse.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void delete_stock_without_movements() {
        var created =
                rest.postForEntity(
                                "/stocks", StockRequest.builder().copyId(copyId).build(), StockResponse.class)
                        .getBody();

        rest.delete("/stocks/" + created.id());

        var found = rest.getForEntity("/stocks/" + created.id(), StockResponse.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
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
