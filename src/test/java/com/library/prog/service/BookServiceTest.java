package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.BookRequest;
import com.library.prog.model.Book;
import com.library.prog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @InjectMocks private BookService bookService;

  @Test
  void findAll_returns_all_books() {
    var book = buildBook("Test Title");
    when(bookRepository.findAll()).thenReturn(List.of(book));

    var result = bookService.findAll();

    assertEquals(1, result.size());
    assertEquals("Test Title", result.getFirst().title());
  }

  @Test
  void findById_returns_book_when_found() {
    var book = buildBook("Found Book");
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

    var result = bookService.findById(book.getId());

    assertEquals("Found Book", result.title());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(bookRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> bookService.findById(id));
  }

  @Test
  void create_saves_and_returns_book() {
    var request = BookRequest.builder().title("New Book").summary("A summary").build();
    var savedBook = buildBook("New Book");
    savedBook.setSummary("A summary");
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    var result = bookService.create(request);

    assertEquals("New Book", result.title());
    assertEquals("A summary", result.summary());
    assertEquals("English", result.language());
    assertNotNull(result.id());
  }

  @Test
  void create_uses_default_language_when_null() {
    var request = BookRequest.builder().title("No Lang").build();
    var captor = ArgumentCaptor.forClass(Book.class);
    when(bookRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

    bookService.create(request);

    assertEquals("English", captor.getValue().getLanguage());
  }

  @Test
  void update_modifies_existing_book() {
    var existing = buildBook("Old Title");
    var request =
        BookRequest.builder()
            .title("Updated Title")
            .summary("New summary")
            .language("French")
            .build();
    when(bookRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = bookService.update(existing.getId(), request);

    assertEquals("Updated Title", result.title());
    assertEquals("New summary", result.summary());
    assertEquals("French", result.language());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = BookRequest.builder().title("Any").build();
    when(bookRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> bookService.update(id, request));
  }

  @Test
  void search_returns_matching_books() {
    var book = buildBook("Searched Book");
    when(bookRepository.search("Search", null, null)).thenReturn(List.of(book));

    var result = bookService.search("Search", null, null);

    assertEquals(1, result.size());
    assertEquals("Searched Book", result.getFirst().title());
  }

  @Test
  void search_returns_all_when_params_null() {
    var book1 = buildBook("Book A");
    var book2 = buildBook("Book B");
    when(bookRepository.search(null, null, null)).thenReturn(List.of(book1, book2));

    var result = bookService.search(null, null, null);

    assertEquals(2, result.size());
  }

  @Test
  void findByAuthorId_returns_books() {
    var book = buildBook("Author Book");
    var authorId = UUID.randomUUID();
    when(bookRepository.findByAuthorId(authorId)).thenReturn(List.of(book));

    var result = bookService.findByAuthorId(authorId);

    assertEquals(1, result.size());
    assertEquals("Author Book", result.getFirst().title());
  }

  @Test
  void findByCategoryId_returns_books() {
    var book = buildBook("Category Book");
    var categoryId = UUID.randomUUID();
    when(bookRepository.findByCategoryId(categoryId)).thenReturn(List.of(book));

    var result = bookService.findByCategoryId(categoryId);

    assertEquals(1, result.size());
    assertEquals("Category Book", result.getFirst().title());
  }

  @Test
  void delete_removes_book_when_exists() {
    var id = UUID.randomUUID();
    when(bookRepository.existsById(id)).thenReturn(true);

    bookService.delete(id);

    verify(bookRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(bookRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> bookService.delete(id));
  }

  private Book buildBook(String title) {
    return Book.builder()
        .id(UUID.randomUUID())
        .title(title)
        .language("English")
        .createdAt(Instant.now())
        .build();
  }
}
