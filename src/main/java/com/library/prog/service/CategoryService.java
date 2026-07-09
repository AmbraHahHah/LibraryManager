package com.library.prog.service;

import com.library.prog.dto.request.CategoryRequest;
import com.library.prog.dto.response.CategoryChildResponse;
import com.library.prog.dto.response.CategoryResponse;
import com.library.prog.model.Category;
import com.library.prog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<CategoryResponse> findAll() {
    return categoryRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse findById(UUID id) {
    return categoryRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> findRootCategories() {
    return categoryRepository.findAll().stream()
        .filter(c -> c.getParentCategory() == null)
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    Category parent = null;
    if (request.parentId() != null) {
      parent =
          categoryRepository
              .findById(request.parentId())
              .orElseThrow(
                  () -> new EntityNotFoundException("Parent category not found: " + request.parentId()));
    }

    var category =
        Category.builder()
            .name(request.name())
            .description(request.description())
            .parentCategory(parent)
            .build();
    return toResponse(categoryRepository.save(category));
  }

  @Transactional
  public CategoryResponse update(UUID id, CategoryRequest request) {
    var category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));

    Category parent = null;
    if (request.parentId() != null) {
      parent =
          categoryRepository
              .findById(request.parentId())
              .orElseThrow(
                  () -> new EntityNotFoundException("Parent category not found: " + request.parentId()));
    }

    category.setName(request.name());
    category.setDescription(request.description());
    category.setParentCategory(parent);
    return toResponse(categoryRepository.save(category));
  }

  @Transactional
  public void delete(UUID id) {
    if (!categoryRepository.existsById(id)) {
      throw new EntityNotFoundException("Category not found: " + id);
    }
    categoryRepository.deleteById(id);
  }

  private CategoryResponse toResponse(Category category) {
    var parent = category.getParentCategory();
    var children =
        category.getSubCategories().stream()
            .map(c -> new CategoryChildResponse(c.getId(), c.getName()))
            .toList();
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .description(category.getDescription())
        .parentId(parent != null ? parent.getId() : null)
        .parentName(parent != null ? parent.getName() : null)
        .subCategories(children)
        .build();
  }
}
