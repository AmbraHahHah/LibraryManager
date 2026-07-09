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
      RevenueController.class,
      AuthorController.class,
      CategoryController.class,
      ClientController.class,
      ReviewController.class,
      OrderLineController.class,
      OrderController.class
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
  @MockBean private AuthorService authorService;
  @MockBean private CategoryService categoryService;
  @MockBean private ClientService clientService;
  @MockBean private ReviewService reviewService;
  @MockBean private OrderLineService orderLineService;
  @MockBean private OrderService orderService;

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
  void book_search() throws Exception {
    var response = new BookResponse(UUID.randomUUID(), "Found", null, "English", Instant.now());
    when(bookService.search("test", null, null)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/books/search?title=test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Found"));
  }

  @Test
  void book_search_all_params() throws Exception {
    var response = new BookResponse(UUID.randomUUID(), "Match", null, "English", Instant.now());
    when(bookService.search("Match", "Hugo", "Fiction")).thenReturn(List.of(response));

    mockMvc
        .perform(get("/books/search?title=Match&author=Hugo&category=Fiction"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Match"));
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
  void book_getCopies() throws Exception {
    var bookId = UUID.randomUUID();
    var response =
        new CopyResponse(
            UUID.randomUUID(), "ISBN", FormatEnum.PAPERBACK, BigDecimal.TEN, 100, null, null, null,
            bookId, null);
    when(copyService.findByBookId(bookId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/books/" + bookId + "/copies"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void book_getReviews() throws Exception {
    var bookId = UUID.randomUUID();
    var response = ReviewResponse.builder().id(UUID.randomUUID()).rating(5).build();
    when(reviewService.findByBookId(bookId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/books/" + bookId + "/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rating").value(5));
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

  @Test
  void author_findAll() throws Exception {
    var response = new AuthorResponse(UUID.randomUUID(), "Hugo", null, null, null, null);
    when(authorService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/authors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].lastName").value("Hugo"));
  }

  @Test
  void author_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = new AuthorResponse(id, "Found", null, null, null, null);
    when(authorService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/authors/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Found"));
  }

  @Test
  void author_create() throws Exception {
    var request = new AuthorRequest("Hugo", null, null, null, null);
    var response = new AuthorResponse(UUID.randomUUID(), "Hugo", null, null, null, null);
    when(authorService.create(any(AuthorRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.lastName").value("Hugo"));
  }

  @Test
  void author_update() throws Exception {
    var id = UUID.randomUUID();
    var request = new AuthorRequest("Updated", null, null, null, null);
    var response = new AuthorResponse(id, "Updated", null, null, null, null);
    when(authorService.update(any(UUID.class), any(AuthorRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/authors/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Updated"));
  }

  @Test
  void author_getBooks() throws Exception {
    var authorId = UUID.randomUUID();
    var response = new BookResponse(UUID.randomUUID(), "Book Title", null, "English", Instant.now());
    when(bookService.findByAuthorId(authorId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/authors/" + authorId + "/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Book Title"));
  }

  @Test
  void author_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/authors/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void category_findAll() throws Exception {
    var response = CategoryResponse.builder().id(UUID.randomUUID()).name("Fiction").build();
    when(categoryService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Fiction"));
  }

  @Test
  void category_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = CategoryResponse.builder().id(id).name("Found").build();
    when(categoryService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/categories/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Found"));
  }

  @Test
  void category_findRoots() throws Exception {
    var response = CategoryResponse.builder().id(UUID.randomUUID()).name("Root").build();
    when(categoryService.findRootCategories()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/categories/roots"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Root"));
  }

  @Test
  void category_create() throws Exception {
    var request = new CategoryRequest("Fiction", null, null);
    var response = CategoryResponse.builder().id(UUID.randomUUID()).name("Fiction").build();
    when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Fiction"));
  }

  @Test
  void category_update() throws Exception {
    var id = UUID.randomUUID();
    var request = new CategoryRequest("Updated", null, null);
    var response = CategoryResponse.builder().id(id).name("Updated").build();
    when(categoryService.update(any(UUID.class), any(CategoryRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/categories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated"));
  }

  @Test
  void category_getBooks() throws Exception {
    var categoryId = UUID.randomUUID();
    var response = new BookResponse(UUID.randomUUID(), "Cat Book", null, "English", Instant.now());
    when(bookService.findByCategoryId(categoryId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/categories/" + categoryId + "/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Cat Book"));
  }

  @Test
  void category_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/categories/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void client_findAll() throws Exception {
    var response = ClientResponse.builder().id(UUID.randomUUID()).lastName("Doe").build();
    when(clientService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/clients"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].lastName").value("Doe"));
  }

  @Test
  void client_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = ClientResponse.builder().id(id).lastName("Found").build();
    when(clientService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/clients/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Found"));
  }

  @Test
  void client_create() throws Exception {
    var request =
        ClientRequest.builder().lastName("Doe").firstName("John").email("john@test.com").build();
    var response = ClientResponse.builder().id(UUID.randomUUID()).lastName("Doe").build();
    when(clientService.create(any(ClientRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.lastName").value("Doe"));
  }

  @Test
  void client_update() throws Exception {
    var id = UUID.randomUUID();
    var request =
        ClientRequest.builder().lastName("Updated").firstName("New").email("new@test.com").build();
    var response = ClientResponse.builder().id(id).lastName("Updated").build();
    when(clientService.update(any(UUID.class), any(ClientRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/clients/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastName").value("Updated"));
  }

  @Test
  void client_getOrders() throws Exception {
    var clientId = UUID.randomUUID();
    var response = OrderResponse.builder().id(UUID.randomUUID()).status(OrderStatusEnum.PENDING).build();
    when(orderService.findByClientId(clientId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/clients/" + clientId + "/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  void client_getReviews() throws Exception {
    var clientId = UUID.randomUUID();
    var response = ReviewResponse.builder().id(UUID.randomUUID()).rating(3).build();
    when(reviewService.findByClientId(clientId)).thenReturn(List.of(response));

    mockMvc
        .perform(get("/clients/" + clientId + "/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rating").value(3));
  }

  @Test
  void client_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/clients/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void review_findAll() throws Exception {
    var response = ReviewResponse.builder().id(UUID.randomUUID()).rating(4).build();
    when(reviewService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rating").value(4));
  }

  @Test
  void review_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = ReviewResponse.builder().id(id).rating(5).build();
    when(reviewService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/reviews/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating").value(5));
  }

  @Test
  void review_create() throws Exception {
    var request = new ReviewRequest(UUID.randomUUID(), UUID.randomUUID(), 4, "Good");
    var response = ReviewResponse.builder().id(UUID.randomUUID()).rating(4).build();
    when(reviewService.create(any(ReviewRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rating").value(4));
  }

  @Test
  void review_update() throws Exception {
    var id = UUID.randomUUID();
    var request = new ReviewRequest(UUID.randomUUID(), UUID.randomUUID(), 5, "Great");
    var response = ReviewResponse.builder().id(id).rating(5).build();
    when(reviewService.update(any(UUID.class), any(ReviewRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/reviews/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rating").value(5));
  }

  @Test
  void review_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/reviews/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void orderLine_findAll() throws Exception {
    var response = OrderLineResponse.builder().id(UUID.randomUUID()).quantity(3).build();
    when(orderLineService.findAll()).thenReturn(List.of(response));

    mockMvc
        .perform(get("/order-lines"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].quantity").value(3));
  }

  @Test
  void orderLine_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = OrderLineResponse.builder().id(id).quantity(5).build();
    when(orderLineService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/order-lines/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(5));
  }

  @Test
  void orderLine_findByOrderId() throws Exception {
    var orderId = UUID.randomUUID();
    var response = OrderLineResponse.builder().id(UUID.randomUUID()).quantity(2).build();
    when(orderLineService.findByOrderId(orderId)).thenReturn(List.of(response));

    mockMvc.perform(get("/order-lines/by-order?orderId=" + orderId)).andExpect(status().isOk());
  }

  @Test
  void orderLine_create() throws Exception {
    var request =
        new OrderLineRequest(
            UUID.randomUUID(), UUID.randomUUID(), 2, BigDecimal.TEN, BigDecimal.ZERO);
    var response = OrderLineResponse.builder().id(UUID.randomUUID()).quantity(2).build();
    when(orderLineService.create(any(OrderLineRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/order-lines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void orderLine_update() throws Exception {
    var id = UUID.randomUUID();
    var request =
        new OrderLineRequest(
            UUID.randomUUID(), UUID.randomUUID(), 5, BigDecimal.valueOf(20), BigDecimal.ZERO);
    var response = OrderLineResponse.builder().id(id).quantity(5).build();
    when(orderLineService.update(any(UUID.class), any(OrderLineRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/order-lines/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(5));
  }

  @Test
  void orderLine_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/order-lines/" + id)).andExpect(status().isNoContent());
  }

  @Test
  void order_findAll() throws Exception {
    var response =
        OrderResponse.builder().id(UUID.randomUUID()).status(OrderStatusEnum.PENDING).build();
    when(orderService.findAll()).thenReturn(List.of(response));

    mockMvc.perform(get("/orders")).andExpect(status().isOk());
  }

  @Test
  void order_findById() throws Exception {
    var id = UUID.randomUUID();
    var response = OrderResponse.builder().id(id).status(OrderStatusEnum.PENDING).build();
    when(orderService.findById(id)).thenReturn(response);

    mockMvc
        .perform(get("/orders/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void order_create() throws Exception {
    var request =
        OrderRequest.builder()
            .clientId(UUID.randomUUID())
            .lines(
                List.of(OrderLineRequest.builder().copyId(UUID.randomUUID()).quantity(1).build()))
            .build();
    var response =
        OrderResponse.builder().id(UUID.randomUUID()).status(OrderStatusEnum.PENDING).build();
    when(orderService.create(any(OrderRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void order_confirm() throws Exception {
    var id = UUID.randomUUID();
    var response = OrderResponse.builder().id(id).status(OrderStatusEnum.CONFIRMED).build();
    when(orderService.confirm(id)).thenReturn(response);

    mockMvc
        .perform(patch("/orders/" + id + "/confirm"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void order_cancel() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(patch("/orders/" + id + "/cancel")).andExpect(status().isNoContent());
  }

  @Test
  void order_delete() throws Exception {
    var id = UUID.randomUUID();
    mockMvc.perform(delete("/orders/" + id)).andExpect(status().isNoContent());
  }
}
