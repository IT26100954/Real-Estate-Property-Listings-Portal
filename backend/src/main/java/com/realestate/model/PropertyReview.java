package com.realestate.model;

import java.util.Map;

public final class PropertyReview extends Review {
  public PropertyReview(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String displayReview() {
    return "Property " + getTargetId() + ": " + getRating() + "/5 — " + getComment();
  }

  public String summary() {
    return displayReview();
  }
}
