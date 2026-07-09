package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.AuthorRequest;
import com.library.prog.model.Author;
import com.library.prog.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
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
class AuthorServiceTest {

  @Mock private AuthorRepository authorRepository;

  @InjectMocks private AuthorService authorService;

  @Test
  void findAll_returns_all_authors() {
    var author = buildAuthor("Hugo");
    when(authorRepository.findAll()).thenReturn(List.of(author));

    var result = authorService.findAll();

    assertEquals(1, result.size());
    assertEquals("Hugo", result.getFirst().lastName());
  }

  @Test
  void findById_returns_author_when_found() {
    var author = buildAuthor("Hugo");
    when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));

    var result = authorService.findById(author.getId());

    assertEquals("Hugo", result.lastName());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(authorRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> authorService.findById(id));
  }

  @Test
  void create_saves_and_returns_author() {
    var request =
        AuthorRequest.builder()
            .lastName("Hugo")
            .firstName("Victor")
            .nationality("French")
            .birthDate(LocalDate.of(1802, 2, 26))
            .build();
    var savedAuthor = buildAuthor("Hugo");
    savedAuthor.setFirstName("Victor");
    savedAuthor.setNationality("French");
    savedAuthor.setBirthDate(LocalDate.of(1802, 2, 26));
    when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

    var result = authorService.create(request);

    assertEquals("Hugo", result.lastName());
    assertEquals("Victor", result.firstName());
    assertEquals("French", result.nationality());
    assertEquals(LocalDate.of(1802, 2, 26), result.birthDate());
    assertNotNull(result.id());
  }

  @Test
  void create_with_minimal_fields() {
    var request = AuthorRequest.builder().lastName("Minimal").build();
    var captor = ArgumentCaptor.forClass(Author.class);
    when(authorRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    authorService.create(request);

    assertEquals("Minimal", captor.getValue().getLastName());
    assertNull(captor.getValue().getFirstName());
    assertNull(captor.getValue().getNationality());
    assertNull(captor.getValue().getBirthDate());
  }

  @Test
  void update_modifies_existing_author() {
    var existing = buildAuthor("Old Name");
    var request =
        AuthorRequest.builder()
            .lastName("Updated")
            .firstName("New")
            .biography("Updated bio")
            .build();
    when(authorRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(authorRepository.save(any(Author.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = authorService.update(existing.getId(), request);

    assertEquals("Updated", result.lastName());
    assertEquals("New", result.firstName());
    assertEquals("Updated bio", result.biography());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = AuthorRequest.builder().lastName("Any").build();
    when(authorRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> authorService.update(id, request));
  }

  @Test
  void delete_removes_author_when_exists() {
    var id = UUID.randomUUID();
    when(authorRepository.existsById(id)).thenReturn(true);

    authorService.delete(id);

    verify(authorRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(authorRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> authorService.delete(id));
  }

  private Author buildAuthor(String lastName) {
    return Author.builder().id(UUID.randomUUID()).lastName(lastName).build();
  }
}
