package com.realestate.model;

import java.util.Map;

public final class AgentReview extends Review {
  public AgentReview(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String displayReview() {
    return "Agent " + getTargetId() + ": " + getRating() + "/5 — " + getComment();
  }

  public String summary() {
    return displayReview();
  }
}
