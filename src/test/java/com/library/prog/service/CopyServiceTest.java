package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.CopyRequest;
import com.library.prog.model.Book;
import com.library.prog.model.Copy;
import com.library.prog.model.Editor;
import com.library.prog.model.FormatEnum;
import com.library.prog.repository.BookRepository;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.EditorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class CopyServiceTest {

  @Mock private CopyRepository copyRepository;
  @Mock private BookRepository bookRepository;
  @Mock private EditorRepository editorRepository;

  @InjectMocks private CopyService copyService;

  @Test
  void findAll_returns_all_copies() {
    var book = buildBook();
    var copy = buildCopy("1234567890", book, null);
    when(copyRepository.findAll()).thenReturn(List.of(copy));

    var result = copyService.findAll();

    assertEquals(1, result.size());
    assertEquals("1234567890", result.getFirst().isbn());
  }

  @Test
  void findById_returns_copy_when_found() {
    var book = buildBook();
    var copy = buildCopy("1234567890", book, null);
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));

    var result = copyService.findById(copy.getId());

    assertEquals("1234567890", result.isbn());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(copyRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.findById(id));
  }

  @Test
  void create_saves_and_returns_copy() {
    var book = buildBook();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .price(new BigDecimal("12.99"))
            .pageCount(200)
            .publicationDate(LocalDate.of(2020, 1, 1))
            .bookId(book.getId())
            .build();
    var savedCopy = buildCopy("9782070612758", book, null);
    savedCopy.setPrice(new BigDecimal("12.99"));
    savedCopy.setPageCount(200);
    savedCopy.setPublicationDate(LocalDate.of(2020, 1, 1));
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(copyRepository.save(any(Copy.class))).thenReturn(savedCopy);

    var result = copyService.create(request);

    assertEquals("9782070612758", result.isbn());
    assertEquals(FormatEnum.PAPERBACK, result.format());
    assertEquals(0, new BigDecimal("12.99").compareTo(result.price()));
    assertEquals(200, result.pageCount());
    assertEquals(LocalDate.of(2020, 1, 1), result.publicationDate());
    assertEquals(book.getId(), result.bookId());
    assertNull(result.publisherId());
    assertNotNull(result.id());
  }

  @Test
  void create_with_publisher() {
    var book = buildBook();
    var editor = buildEditor();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.HARDCOVER)
            .bookId(book.getId())
            .publisherId(editor.getId())
            .build();
    var savedCopy = buildCopy("9782070612758", book, editor);
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(editorRepository.findById(editor.getId())).thenReturn(Optional.of(editor));
    when(copyRepository.save(any(Copy.class))).thenReturn(savedCopy);

    var result = copyService.create(request);

    assertEquals(editor.getId(), result.publisherId());
  }

  @Test
  void create_throws_when_book_not_found() {
    var bookId = UUID.randomUUID();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .bookId(bookId)
            .build();
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.create(request));
  }

  @Test
  void create_throws_when_editor_not_found() {
    var book = buildBook();
    var editorId = UUID.randomUUID();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .bookId(book.getId())
            .publisherId(editorId)
            .build();
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(editorRepository.findById(editorId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.create(request));
  }

  @Test
  void create_uses_default_price_when_null() {
    var book = buildBook();
    var request =
        CopyRequest.builder()
            .isbn("9782070612758")
            .format(FormatEnum.PAPERBACK)
            .bookId(book.getId())
            .build();
    var captor = ArgumentCaptor.forClass(Copy.class);
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(copyRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

    copyService.create(request);

    assertEquals(0, BigDecimal.ZERO.compareTo(captor.getValue().getPrice()));
  }

  @Test
  void update_modifies_existing_copy() {
    var book = buildBook();
    var existing = buildCopy("OLDISBN", book, null);
    var request =
        CopyRequest.builder()
            .isbn("NEWISBN")
            .format(FormatEnum.EBOOK)
            .price(new BigDecimal("9.99"))
            .pageCount(150)
            .bookId(book.getId())
            .build();
    when(copyRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(copyRepository.save(any(Copy.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = copyService.update(existing.getId(), request);

    assertEquals("NEWISBN", result.isbn());
    assertEquals(FormatEnum.EBOOK, result.format());
    assertEquals(0, new BigDecimal("9.99").compareTo(result.price()));
    assertEquals(150, result.pageCount());
  }

  @Test
  void update_with_publisher() {
    var book = buildBook();
    var editor = buildEditor();
    var existing = buildCopy("OLDISBN", book, null);
    var request =
        CopyRequest.builder()
            .isbn("NEWISBN")
            .format(FormatEnum.HARDCOVER)
            .price(new BigDecimal("19.99"))
            .pageCount(300)
            .bookId(book.getId())
            .publisherId(editor.getId())
            .build();
    when(copyRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(editorRepository.findById(editor.getId())).thenReturn(Optional.of(editor));
    when(copyRepository.save(any(Copy.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = copyService.update(existing.getId(), request);

    assertEquals(editor.getId(), result.publisherId());
    assertEquals(FormatEnum.HARDCOVER, result.format());
  }

  @Test
  void update_throws_when_copy_not_found() {
    var id = UUID.randomUUID();
    var book = buildBook();
    var request =
        CopyRequest.builder()
            .isbn("ISBN")
            .format(FormatEnum.PAPERBACK)
            .bookId(book.getId())
            .build();
    when(copyRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.update(id, request));
  }

  @Test
  void update_throws_when_book_not_found() {
    var copy = buildCopy("ISBN", buildBook(), null);
    var bookId = UUID.randomUUID();
    var request =
        CopyRequest.builder().isbn("ISBN").format(FormatEnum.PAPERBACK).bookId(bookId).build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.update(copy.getId(), request));
  }

  @Test
  void update_throws_when_editor_not_found() {
    var book = buildBook();
    var editorId = UUID.randomUUID();
    var copy = buildCopy("ISBN", buildBook(), null);
    var request =
        CopyRequest.builder()
            .isbn("ISBN")
            .format(FormatEnum.PAPERBACK)
            .bookId(book.getId())
            .publisherId(editorId)
            .build();
    when(copyRepository.findById(copy.getId())).thenReturn(Optional.of(copy));
    when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
    when(editorRepository.findById(editorId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> copyService.update(copy.getId(), request));
  }

  @Test
  void delete_removes_copy_when_exists() {
    var id = UUID.randomUUID();
    when(copyRepository.existsById(id)).thenReturn(true);

    copyService.delete(id);

    verify(copyRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(copyRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> copyService.delete(id));
  }

  private Book buildBook() {
    return Book.builder()
        .id(UUID.randomUUID())
        .title("Test Book")
        .language("English")
        .createdAt(Instant.now())
        .build();
  }

  private Editor buildEditor() {
    return Editor.builder().id(UUID.randomUUID()).name("Test Editor").build();
  }

  private Copy buildCopy(String isbn, Book book, Editor editor) {
    return Copy.builder()
        .id(UUID.randomUUID())
        .isbn(isbn)
        .format(FormatEnum.PAPERBACK)
        .price(BigDecimal.ZERO)
        .book(book)
        .publisher(editor)
        .build();
  }
}
