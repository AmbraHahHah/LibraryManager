package com.library.prog.conf;

import com.library.prog.PojaGenerated;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

@PojaGenerated
public class PostgresConf {

  private final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:13.9");

  void start() {
    postgres.start();
  }

  void stop() {
    postgres.stop();
  }

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("SPRING_DATASOURCE_URL", postgres::getJdbcUrl);
    registry.add("SPRING_DATASOURCE_USERNAME", postgres::getUsername);
    registry.add("SPRING_DATASOURCE_PASSWORD", postgres::getPassword);
  }
}
