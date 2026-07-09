package com.library.prog.endpoint.rest.controller;

import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.dto.response.BookStockResponse;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.dto.response.ReviewResponse;
import com.library.prog.dto.response.StockByEditionResponse;
import com.library.prog.service.BookService;
import com.library.prog.service.CopyService;
import com.library.prog.service.ReviewService;
import com.library.prog.service.StockService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;
  private final StockService stockService;
  private final CopyService copyService;
  private final ReviewService reviewService;

  @GetMapping
  public List<BookResponse> findAll() {
    return bookService.findAll();
  }

  @GetMapping("/search")
  public List<BookResponse> search(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String author,
      @RequestParam(required = false) String category) {
    return bookService.search(title, author, category);
  }

  @GetMapping("/{id}")
  public BookResponse findById(@PathVariable UUID id) {
    return bookService.findById(id);
  }

  @GetMapping("/{bookId}/copies")
  public List<CopyResponse> getCopies(@PathVariable UUID bookId) {
    return copyService.findByBookId(bookId);
  }

  @GetMapping("/{bookId}/reviews")
  public List<ReviewResponse> getReviews(@PathVariable UUID bookId) {
    return reviewService.findByBookId(bookId);
  }

  @GetMapping("/{bookId}/stock")
  public BookStockResponse getBookStock(@PathVariable UUID bookId) {
    return stockService.getBookStock(bookId);
  }

  @GetMapping("/{bookId}/stock-by-edition")
  public StockByEditionResponse getStockByEdition(@PathVariable UUID bookId) {
    return stockService.getStockByEdition(bookId);
  }

  @PostMapping
  public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
    var response = bookService.create(request);
    return ResponseEntity.created(URI.create("/books/" + response.id())).body(response);
  }

  @PutMapping("/{id}")
  public BookResponse update(@PathVariable UUID id, @Valid @RequestBody BookRequest request) {
    return bookService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    bookService.delete(id);
  }
}
