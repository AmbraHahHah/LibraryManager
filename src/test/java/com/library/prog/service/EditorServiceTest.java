package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.EditorRequest;
import com.library.prog.model.Editor;
import com.library.prog.repository.EditorRepository;
import jakarta.persistence.EntityNotFoundException;
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
class EditorServiceTest {

  @Mock private EditorRepository editorRepository;

  @InjectMocks private EditorService editorService;

  @Test
  void findAll_returns_all_editors() {
    var editor = buildEditor("Test Editor");
    when(editorRepository.findAll()).thenReturn(List.of(editor));

    var result = editorService.findAll();

    assertEquals(1, result.size());
    assertEquals("Test Editor", result.getFirst().name());
  }

  @Test
  void findById_returns_editor_when_found() {
    var editor = buildEditor("Found Editor");
    when(editorRepository.findById(editor.getId())).thenReturn(Optional.of(editor));

    var result = editorService.findById(editor.getId());

    assertEquals("Found Editor", result.name());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(editorRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> editorService.findById(id));
  }

  @Test
  void create_saves_and_returns_editor() {
    var request =
        EditorRequest.builder()
            .name("New Editor")
            .address("123 Street")
            .email("editor@test.com")
            .country("France")
            .build();
    var savedEditor = buildEditor("New Editor");
    savedEditor.setAddress("123 Street");
    savedEditor.setEmail("editor@test.com");
    savedEditor.setCountry("France");
    when(editorRepository.save(any(Editor.class))).thenReturn(savedEditor);

    var result = editorService.create(request);

    assertEquals("New Editor", result.name());
    assertEquals("123 Street", result.address());
    assertEquals("editor@test.com", result.email());
    assertEquals("France", result.country());
    assertNotNull(result.id());
  }

  @Test
  void create_with_minimal_fields() {
    var request = EditorRequest.builder().name("Minimal Editor").build();
    var captor = ArgumentCaptor.forClass(Editor.class);
    when(editorRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    editorService.create(request);

    assertEquals("Minimal Editor", captor.getValue().getName());
    assertNull(captor.getValue().getAddress());
    assertNull(captor.getValue().getEmail());
    assertNull(captor.getValue().getCountry());
  }

  @Test
  void update_modifies_existing_editor() {
    var existing = buildEditor("Old Name");
    var request =
        EditorRequest.builder()
            .name("Updated Name")
            .address("New Address")
            .email("new@test.com")
            .country("USA")
            .build();
    when(editorRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(editorRepository.save(any(Editor.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = editorService.update(existing.getId(), request);

    assertEquals("Updated Name", result.name());
    assertEquals("New Address", result.address());
    assertEquals("new@test.com", result.email());
    assertEquals("USA", result.country());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = EditorRequest.builder().name("Any").build();
    when(editorRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> editorService.update(id, request));
  }

  @Test
  void delete_removes_editor_when_exists() {
    var id = UUID.randomUUID();
    when(editorRepository.existsById(id)).thenReturn(true);

    editorService.delete(id);

    verify(editorRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(editorRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> editorService.delete(id));
  }

  private Editor buildEditor(String name) {
    return Editor.builder().id(UUID.randomUUID()).name(name).build();
  }
}
