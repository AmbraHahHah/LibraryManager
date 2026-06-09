package com.library.prog.service;

import com.library.prog.dto.request.BookRequest;
import com.library.prog.dto.response.BookResponse;
import com.library.prog.model.Book;
import com.library.prog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;

  public List<BookResponse> findAll() {
    return bookRepository.findAll().stream().map(this::toResponse).toList();
  }

  public BookResponse findById(UUID id) {
    return bookRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
  }

  public BookResponse create(BookRequest request) {
    var book =
        Book.builder()
            .title(request.title())
            .summary(request.summary())
            .language(request.language() != null ? request.language() : "English")
            .build();
    return toResponse(bookRepository.save(book));
  }

  public BookResponse update(UUID id, BookRequest request) {
    var book =
        bookRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
    book.setTitle(request.title());
    book.setSummary(request.summary());
    book.setLanguage(request.language() != null ? request.language() : "English");
    return toResponse(bookRepository.save(book));
  }

  public void delete(UUID id) {
    if (!bookRepository.existsById(id)) {
      throw new EntityNotFoundException("Book not found: " + id);
    }
    bookRepository.deleteById(id);
  }

  private BookResponse toResponse(Book book) {
    return BookResponse.builder()
        .id(book.getId())
        .title(book.getTitle())
        .summary(book.getSummary())
        .language(book.getLanguage())
        .createdAt(book.getCreatedAt())
        .build();
  }
}
