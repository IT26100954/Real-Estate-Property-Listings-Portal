package com.realestate.model;

import java.util.*;

/** Encapsulates immutable identity and a defensive copy of persisted fields. */
public abstract class Entity {
  private final String id;
  private final Map<String, String> fields;

  protected Entity(String id, Map<String, String> fields) {
    this.id = Objects.requireNonNull(id);
    this.fields = new LinkedHashMap<>(fields);
  }

  public final String getId() {
    return id;
  }

  public final String get(String key) {
    return fields.getOrDefault(key, "");
  }

  public final Map<String, String> fields() {
    return Collections.unmodifiableMap(fields);
  }

  protected final double number(String key) {
    return Double.parseDouble(get(key));
  }

  public String label() {
    for (String key : List.of("title", "fullName", "username", "comment"))
      if (!get(key).isBlank()) return get(key);
    return id;
  }

  public abstract String summary();
}
