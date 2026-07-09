package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.ReviewRequest;
import com.library.prog.model.*;
import com.library.prog.repository.BookRepository;
import com.library.prog.repository.ClientRepository;
import com.library.prog.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private BookRepository bookRepository;
  @Mock private ClientRepository clientRepository;

  @InjectMocks private ReviewService reviewService;

  @Test
  void findAll_returns_all_reviews() {
    var review = buildReview();
    when(reviewRepository.findAll()).thenReturn(List.of(review));

    var result = reviewService.findAll();

    assertEquals(1, result.size());
    assertEquals(4, result.getFirst().rating());
  }

  @Test
  void findById_returns_review_when_found() {
    var review = buildReview();
    when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

    var result = reviewService.findById(review.getId());

    assertEquals(4, result.rating());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(reviewRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> reviewService.findById(id));
  }

  @Test
  void create_saves_and_returns_review() {
    var book = buildBook();
    var client = buildClient();
    var request =
        ReviewRequest.builder()
            .bookId(book.getId())
            .clientId(client.getId())
            .rating(5)
            .comment("Excellent!")
            .build();
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = reviewService.create(request);

    assertEquals(5, result.rating());
    assertEquals("Excellent!", result.comment());
    assertEquals(book.getId(), result.bookId());
    assertEquals(client.getId(), result.clientId());
    assertFalse(result.valid());
  }

  @Test
  void create_throws_when_book_not_found() {
    var bookId = UUID.randomUUID();
    var request =
        ReviewRequest.builder().bookId(bookId).clientId(UUID.randomUUID()).rating(3).build();
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> reviewService.create(request));
  }

  @Test
  void create_throws_when_client_not_found() {
    var book = buildBook();
    var clientId = UUID.randomUUID();
    var request =
        ReviewRequest.builder().bookId(book.getId()).clientId(clientId).rating(3).build();
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> reviewService.create(request));
  }

  @Test
  void update_modifies_existing_review() {
    var existing = buildReview();
    var newBook = buildBook();
    var newClient = buildClient();
    var request =
        ReviewRequest.builder()
            .bookId(newBook.getId())
            .clientId(newClient.getId())
            .rating(2)
            .comment("Disappointing")
            .build();
    when(reviewRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(bookRepository.findById(newBook.getId())).thenReturn(Optional.of(newBook));
    when(clientRepository.findById(newClient.getId())).thenReturn(Optional.of(newClient));
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = reviewService.update(existing.getId(), request);

    assertEquals(2, result.rating());
    assertEquals("Disappointing", result.comment());
    assertEquals(newBook.getId(), result.bookId());
    assertEquals(newClient.getId(), result.clientId());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request =
        ReviewRequest.builder().bookId(UUID.randomUUID()).clientId(UUID.randomUUID()).rating(3).build();
    when(reviewRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> reviewService.update(id, request));
  }

  @Test
  void delete_removes_review_when_exists() {
    var id = UUID.randomUUID();
    when(reviewRepository.existsById(id)).thenReturn(true);

    reviewService.delete(id);

    verify(reviewRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(reviewRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> reviewService.delete(id));
  }

  private Review buildReview() {
    var book = buildBook();
    var client = buildClient();
    return Review.builder()
        .id(UUID.randomUUID())
        .book(book)
        .client(client)
        .rating(4)
        .comment("Good book")
        .valid(false)
        .createdAt(Instant.now())
        .build();
  }

  private Book buildBook() {
    return Book.builder().id(UUID.randomUUID()).title("Test Book").build();
  }

  private Client buildClient() {
    return Client.builder().id(UUID.randomUUID()).lastName("Doe").build();
  }
}
