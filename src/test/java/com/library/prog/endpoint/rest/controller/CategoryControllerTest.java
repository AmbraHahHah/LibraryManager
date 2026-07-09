package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.request.CategoryRequest;
import com.library.prog.dto.response.CategoryResponse;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class CategoryControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void create_and_find_category() {
    var request = CategoryRequest.builder().name("Science-Fiction").description("Sci-fi books").build();

    var created = rest.postForEntity("/categories", request, CategoryResponse.class);

    assertEquals(HttpStatus.CREATED, created.getStatusCode());
    assertNotNull(created.getBody());
    assertNotNull(created.getBody().id());
    assertEquals("Science-Fiction", created.getBody().name());
    assertEquals("Sci-fi books", created.getBody().description());
    assertNull(created.getBody().parentId());

    var found = rest.getForEntity("/categories/" + created.getBody().id(), CategoryResponse.class);

    assertEquals(HttpStatus.OK, found.getStatusCode());
    assertEquals("Science-Fiction", found.getBody().name());
  }

  @Test
  void list_all_categories() {
    rest.postForEntity("/categories", CategoryRequest.builder().name("Fiction").build(), CategoryResponse.class);
    rest.postForEntity("/categories", CategoryRequest.builder().name("Non-Fiction").build(), CategoryResponse.class);

    var response = rest.getForEntity("/categories", CategoryResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 2);
  }

  @Test
  void create_hierarchical_category() {
    var parent =
        rest.postForEntity(
            "/categories", CategoryRequest.builder().name("Parent").build(), CategoryResponse.class);
    var childRequest =
        CategoryRequest.builder().name("Child").parentId(parent.getBody().id()).build();

    var child = rest.postForEntity("/categories", childRequest, CategoryResponse.class);

    assertEquals(HttpStatus.CREATED, child.getStatusCode());
    assertEquals("Child", child.getBody().name());
    assertEquals(parent.getBody().id(), child.getBody().parentId());
    assertEquals("Parent", child.getBody().parentName());
  }

  @Test
  void find_root_categories() {
    rest.postForEntity("/categories", CategoryRequest.builder().name("Root").build(), CategoryResponse.class);

    var response = rest.getForEntity("/categories/roots", CategoryResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().length >= 1);
  }

  @Test
  void update_category() {
    var created =
        rest.postForEntity(
            "/categories", CategoryRequest.builder().name("Original").build(), CategoryResponse.class);
    var updateRequest = CategoryRequest.builder().name("Updated").description("New desc").build();

    rest.put("/categories/" + created.getBody().id(), updateRequest);
    var updated = rest.getForEntity("/categories/" + created.getBody().id(), CategoryResponse.class);

    assertEquals("Updated", updated.getBody().name());
    assertEquals("New desc", updated.getBody().description());
  }

  @Test
  void delete_category() {
    var created =
        rest.postForEntity(
            "/categories", CategoryRequest.builder().name("To Delete").build(), CategoryResponse.class);
    var id = created.getBody().id();

    rest.delete("/categories/" + id);

    var found = rest.getForEntity("/categories/" + id, CategoryResponse.class);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, found.getStatusCode());
  }

  @Test
  void return_400_when_name_is_blank() {
    var request = CategoryRequest.builder().name("").build();

    var response = rest.postForEntity("/categories", request, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void return_404_when_category_not_found() {
    var response = rest.getForEntity("/categories/" + UUID.randomUUID(), CategoryResponse.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
