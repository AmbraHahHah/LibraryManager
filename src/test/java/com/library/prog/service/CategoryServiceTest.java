package com.library.prog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.library.prog.dto.request.CategoryRequest;
import com.library.prog.model.Category;
import com.library.prog.repository.CategoryRepository;
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
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryService categoryService;

  @Test
  void findAll_returns_all_categories() {
    var category = buildCategory("Fiction");
    when(categoryRepository.findAll()).thenReturn(List.of(category));

    var result = categoryService.findAll();

    assertEquals(1, result.size());
    assertEquals("Fiction", result.getFirst().name());
  }

  @Test
  void findById_returns_category_when_found() {
    var category = buildCategory("Fiction");
    when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

    var result = categoryService.findById(category.getId());

    assertEquals("Fiction", result.name());
  }

  @Test
  void findById_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(categoryRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> categoryService.findById(id));
  }

  @Test
  void findRootCategories_returns_only_root_categories() {
    var root = buildCategory("Root");
    var child = buildCategory("Child");
    child.setParentCategory(root);
    when(categoryRepository.findAll()).thenReturn(List.of(root, child));

    var result = categoryService.findRootCategories();

    assertEquals(1, result.size());
    assertEquals("Root", result.getFirst().name());
  }

  @Test
  void create_saves_and_returns_category() {
    var request =
        CategoryRequest.builder().name("Science-Fiction").description("Sci-fi books").build();
    var saved = buildCategory("Science-Fiction");
    saved.setDescription("Sci-fi books");
    when(categoryRepository.save(any(Category.class))).thenReturn(saved);

    var result = categoryService.create(request);

    assertEquals("Science-Fiction", result.name());
    assertEquals("Sci-fi books", result.description());
    assertNull(result.parentId());
    assertTrue(result.subCategories().isEmpty());
  }

  @Test
  void create_with_parent_category() {
    var parent = buildCategory("Parent");
    parent.setId(UUID.randomUUID());
    when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

    var request = CategoryRequest.builder().name("Child").parentId(parent.getId()).build();
    var captor = ArgumentCaptor.forClass(Category.class);
    when(categoryRepository.save(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    categoryService.create(request);

    assertEquals("Child", captor.getValue().getName());
    assertNotNull(captor.getValue().getParentCategory());
    assertEquals(parent.getId(), captor.getValue().getParentCategory().getId());
  }

  @Test
  void create_throws_when_parent_not_found() {
    var parentId = UUID.randomUUID();
    var request = CategoryRequest.builder().name("Child").parentId(parentId).build();
    when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> categoryService.create(request));
  }

  @Test
  void update_modifies_existing_category() {
    var existing = buildCategory("Old");
    var request = CategoryRequest.builder().name("Updated").description("New desc").build();
    when(categoryRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = categoryService.update(existing.getId(), request);

    assertEquals("Updated", result.name());
    assertEquals("New desc", result.description());
  }

  @Test
  void update_throws_when_not_found() {
    var id = UUID.randomUUID();
    var request = CategoryRequest.builder().name("Any").build();
    when(categoryRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> categoryService.update(id, request));
  }

  @Test
  void delete_removes_category_when_exists() {
    var id = UUID.randomUUID();
    when(categoryRepository.existsById(id)).thenReturn(true);

    categoryService.delete(id);

    verify(categoryRepository).deleteById(id);
  }

  @Test
  void delete_throws_when_not_found() {
    var id = UUID.randomUUID();
    when(categoryRepository.existsById(id)).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> categoryService.delete(id));
  }

  @Test
  void toResponse_includes_sub_categories() {
    var parent = buildCategory("Parent");
    var child = buildCategory("Child");
    child.setParentCategory(parent);
    parent.getSubCategories().add(child);

    when(categoryRepository.findAll()).thenReturn(List.of(parent));

    var result = categoryService.findAll();

    assertEquals(1, result.size());
    assertEquals(1, result.getFirst().subCategories().size());
    assertEquals("Child", result.getFirst().subCategories().getFirst().name());
  }

  private Category buildCategory(String name) {
    return Category.builder().id(UUID.randomUUID()).name(name).build();
  }
}
