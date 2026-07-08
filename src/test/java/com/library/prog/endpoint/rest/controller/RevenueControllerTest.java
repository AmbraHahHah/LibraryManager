package com.library.prog.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.library.prog.conf.FacadeIT;
import com.library.prog.dto.response.RevenueByGenreResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Tag("integration")
class RevenueControllerTest extends FacadeIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void get_revenue_by_genre() {
    var response = rest.getForEntity("/revenue/by-genre", RevenueByGenreResponse[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().length > 0);

    for (var entry : response.getBody()) {
      assertNotNull(entry.genre());
      assertNotNull(entry.revenue());
      assertTrue(entry.revenue().compareTo(java.math.BigDecimal.ZERO) > 0);
    }
  }

  @Test
  void get_revenue_by_genre_is_sorted_descending() {
    var response = rest.getForEntity("/revenue/by-genre", RevenueByGenreResponse[].class);

    var data = response.getBody();
    for (int i = 0; i < data.length - 1; i++) {
      assertTrue(
          data[i].revenue().compareTo(data[i + 1].revenue()) >= 0,
          "Revenue should be sorted descending at index " + i);
    }
  }

  @Test
  void get_revenue_by_genre_contains_known_genres() {
    var response = rest.getForEntity("/revenue/by-genre", RevenueByGenreResponse[].class);

    var genres =
        java.util.Arrays.stream(response.getBody()).map(RevenueByGenreResponse::genre).toList();

    assertTrue(genres.contains("Fantasy"));
    assertTrue(genres.contains("Fiction"));
  }
}
