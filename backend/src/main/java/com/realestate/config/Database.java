package com.realestate.config;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
/** Shared JDBC bridge for the existing typed domain DAOs. Spring owns connection pooling. */
@Component
public final class Database {
 private static JdbcTemplate template;
 public Database(JdbcTemplate jdbc) { template=jdbc; }
 public static JdbcTemplate jdbc() {
  if(template==null) throw new IllegalStateException("Database not initialized");
  return template;
 }
}
