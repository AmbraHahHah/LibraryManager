package com.library.prog.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.prog.dto.request.*;
import com.library.prog.dto.response.*;
import com.library.prog.dto.response.StockByEditionResponse.EditionStock;
import com.library.prog.model.*;
import com.library.prog.service.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
      BookController.class,
      CopyController.class,
      EditorController.class,
      StockController.class,
      StockMovementController.class,
      RevenueController.class
    })
class ControllerMockMvcTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private BookService bookService;
  @MockBean private CopyService copyService;
  @MockBean private EditorService editorService;
  @MockBean private StockService stockService;
  @MockBean private StockMovementService stockMovementService;
  @MockBean private RevenueService revenueService;

  @Test
  void book_findAll() throws Exception {
    var response =
        new BookResponse(UUID.randomUUID(), "Title", "Summary", "English", Instant.now());
    when(bookService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Title"));
  }

  @Test
  void book_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = new BookResponse(id, "Found", null, "English", Instant.now());
    when(bookService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/books/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Found"));
  }

  @Test
  void book_create() throws Exception {
    var request = new BookRequest("New Book", "Summary", "French");
    var response =
        new BookResponse(UUID.randomUUID(), "New Book", "Summary", "French", Instant.now());
    when(bookService.create(any(BookRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("New Book"));
  }

  @Test
  void book_update() throws Exception {
    var id = UUID.randomUUID();
    var request = new BookRequest("Updated", "New summary", "English");
    var response = new BookResponse(id, "Updated", "New summary", "English", Instant.now());
    when(bookService.update(any(UUID.class), any(BookRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/books/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated"));
  }

  @Test
  void book_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/books/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void book_getStock() throws Exception {
    var bookId = UUID.randomUUID();
    var response =
        BookStockResponse.builder()
            .totalCopies(2)
            .totalAvailableStock(10)
            .totalReservedQuantity(2)
            .outOfStock(false)
            .build();
    when(stockService.getBookStock(bookId)).thenReturn(response);

    mockMvc
        .perform(get("/books/" + bookId + "/stock"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAvailableStock").value(10));
  }

  @Test
  void book_getStockByEdition() throws Exception {
    var bookId = UUID.randomUUID();
    var response =
        StockByEditionResponse.builder()
            .editions(
                List.of(
                    EditionStock.builder()
                        .format("PAPERBACK")
                        .copyCount(1)
                        .availableStock(5)
                        .reservedQuantity(0)
                        .build()))
            .build();
    when(stockService.getStockByEdition(bookId)).thenReturn(response);

    mockMvc
        .perform(get("/books/" + bookId + "/stock-by-edition"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.editions[0].format").value("PAPERBACK"));
  }

  @Test
  void editor_findAll() throws Exception {
    var response = new EditorResponse(UUID.randomUUID(), "Editor A", null, null, null);
    when(editorService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/editors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void editor_create() throws Exception {
    var request = new EditorRequest("Gallimard", null, null, null);
    var response = new EditorResponse(UUID.randomUUID(), "Gallimard", null, null, null);
    when(editorService.create(any(EditorRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/editors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Gallimard"));
  }

  @Test
  void editor_update() throws Exception {
    var id = UUID.randomUUID();
    var request = new EditorRequest("Updated", null, null, null);
    var response = new EditorResponse(id, "Updated", null, null, null);
    when(editorService.update(any(UUID.class), any(EditorRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/editors/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  void editor_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/editors/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void copy_findAll() throws Exception {
    var response =
        new CopyResponse(
            UUID.randomUUID(),
            "ISBN",
            FormatEnum.PAPERBACK,
            BigDecimal.TEN,
            100,
            null,
            null,
            null,
            UUID.randomUUID(),
            null);
    when(copyService.findAll()).thenReturn(List.of(response));

    mockMvc.perform(get("/copies")).andExpect(status().isOk());
  }

  @Test
  void copy_create() throws Exception {
    var bookId = UUID.randomUUID();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .bookId(bookId)
            .build();
    var response =
        new CopyResponse(
            UUID.randomUUID(),
            "9782070612758",
            FormatEnum.PAPERBACK,
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            bookId,
            null);
    when(copyService.create(any(CopyRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/copies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void copy_getStock() throws Exception {
    var copyId = UUID.randomUUID();
    var response =
        StockResponse.builder()
            .id(UUID.randomUUID())
            .copyId(copyId)
            .availableQuantity(5)
            .reservedQuantity(0)
            .availableStock(5)
            .outOfStock(false)
            .lowStock(false)
            .build();
    when(stockService.findByCopyId(copyId)).thenReturn(response);

    mockMvc
        .perform(get("/copies/" + copyId + "/stock"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.availableQuantity").value(5));
  }

  @Test
  void copy_getMovements() throws Exception {
    var copyId = UUID.randomUUID();
    var response =
        StockMovementResponse.builder().id(UUID.randomUUID()).quantity(5).copyId(copyId).build();
    when(stockMovementService.findByCopyId(copyId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/copies/" + copyId + "/movements"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void stock_findAll() throws Exception {
    var response =
        StockResponse.builder()
            .id(UUID.randomUUID())
            .copyId(UUID.randomUUID())
            .availableQuantity(10)
            .build();
    when(stockService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/stocks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void stock_create() throws Exception {
    var copyId = UUID.randomUUID();
    var request = StockRequest.builder().copyId(copyId).alertThreshold(5).build();
    var response =
        StockResponse.builder()
            .id(UUID.randomUUID())
            .copyId(copyId)
            .availableQuantity(0)
            .alertThreshold(5)
            .build();
    when(stockService.create(any(StockRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void stock_update() throws Exception {
    var id = UUID.randomUUID();
    var copyId = UUID.randomUUID();
    var request = StockRequest.builder().copyId(copyId).alertThreshold(10).build();
    var response =
        StockResponse.builder()
            .id(id)
            .copyId(copyId)
            .availableQuantity(0)
            .alertThreshold(10)
            .build();
    when(stockService.update(any(UUID.class), any(StockRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/stocks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alertThreshold").value(10));
  }

  @Test
  void stock_adjust() throws Exception {
    var id = UUID.randomUUID();
    var request =
        StockAdjustRequest.builder()
            .quantity(5)
            .movementType(MovementTypeEnum.RESTOCK)
            .reason("Restock")
            .build();
    var response =
        StockResponse.builder().id(id).copyId(UUID.randomUUID()).availableQuantity(5).build();
    when(stockService.adjustStock(any(UUID.class), any(StockAdjustRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            patch("/stocks/" + id + "/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.availableQuantity").value(5));
  }

  @Test
  void stock_findByCopyId() throws Exception {
    var copyId = UUID.randomUUID();
    var response = StockResponse.builder().id(UUID.randomUUID()).copyId(copyId).build();
    when(stockService.findByCopyId(copyId)).thenReturn(response);

    mockMvc
        .perform(get("/stocks/by-copy?copyId=" + copyId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.copyId").value(copyId.toString()));
  }

  @Test
  void stock_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/stocks/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void stockMovement_findAll() throws Exception {
    var response =
        StockMovementResponse.builder()
            .id(UUID.randomUUID())
            .quantity(5)
            .copyId(UUID.randomUUID())
            .build();
    when(stockMovementService.findAll()).thenReturn(List.of(response));

    mockMvc.perform(get("/stock-movements")).andExpect(status().isOk());
  }

  @Test
  void stockMovement_findById() throws Exception {
    var id = UUID.randomUUID();
    var response =
        StockMovementResponse.builder().id(id).quantity(5).copyId(UUID.randomUUID()).build();
    when(stockMovementService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/stock-movements/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void stockMovement_findByCopyId() throws Exception {
    var copyId = UUID.randomUUID();
    var response = StockMovementResponse.builder().id(UUID.randomUUID()).copyId(copyId).build();
    when(stockMovementService.findByCopyId(copyId)).thenReturn(List.of(response));

    mockMvc.perform(get("/stock-movements/by-copy?copyId=" + copyId)).andExpect(status().isOk());
  }

  @Test
  void stockMovement_findByOrderId() throws Exception {
    var orderId = UUID.randomUUID();
    var response = StockMovementResponse.builder().id(UUID.randomUUID()).orderId(orderId).build();
    when(stockMovementService.findByOrderId(orderId)).thenReturn(List.of(response));

    mockMvc.perform(get("/stock-movements/by-order?orderId=" + orderId)).andExpect(status().isOk());
  }

  @Test
  void stockMovement_create() throws Exception {
    var copyId = UUID.randomUUID();
    var request =
        StockMovementRequest.builder()
            .quantity(5)
            .movementType(MovementTypeEnum.RESTOCK)
            .copyId(copyId)
            .build();
    var response =
        StockMovementResponse.builder().id(UUID.randomUUID()).quantity(5).copyId(copyId).build();
    when(stockMovementService.create(any(StockMovementRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/stock-movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void stockMovement_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/stock-movements/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void revenue_byGenre() throws Exception {
    var response =
        List.of(
            new RevenueByGenreResponse("Romance", new BigDecimal("1000")),
            new RevenueByGenreResponse("Fantasy", new BigDecimal("500")));
    when(revenueService.getRevenueByGenre()).thenReturn(response);

    mockMvc
        .perform(get("/revenue/by-genre"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }
}
