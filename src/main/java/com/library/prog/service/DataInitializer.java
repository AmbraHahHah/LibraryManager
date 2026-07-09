package com.library.prog.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"preprod", "prod"})
@Slf4j
public class DataInitializer implements CommandLineRunner {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private Environment env;

  @Override
  public void run(String... args) {
    var profiles = env.getActiveProfiles();
    boolean isProd = profiles.length > 0 && "prod".equals(profiles[0]);
    var seedFile = isProd ? "db/seed/seed-prod.sql" : "db/seed/seed-preprod.sql";

    log.info("Cleaning existing seed data...");
    cleanSeedData();

    log.info(
        "Loading seed data from {} (active profile: {})", seedFile, String.join(", ", profiles));

    try {
      var resource = new ClassPathResource(seedFile);
      var sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      jdbcTemplate.execute(sql);
      log.info("Seed data loaded successfully");
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load seed file: " + seedFile, e);
    }
  }

  private void cleanSeedData() {
    jdbcTemplate.execute("DELETE FROM stock_movement");
    jdbcTemplate.execute("DELETE FROM order_line");
    jdbcTemplate.execute("DELETE FROM stock");
    jdbcTemplate.execute("DELETE FROM orders");
    jdbcTemplate.execute("DELETE FROM review");
    jdbcTemplate.execute("DELETE FROM copy");
    jdbcTemplate.execute("DELETE FROM book_category");
    jdbcTemplate.execute("DELETE FROM book_author");
    jdbcTemplate.execute("DELETE FROM book");
    jdbcTemplate.execute("DELETE FROM category");
    jdbcTemplate.execute("DELETE FROM author");
    jdbcTemplate.execute("DELETE FROM editor");
    jdbcTemplate.execute("DELETE FROM client");
  }
}
