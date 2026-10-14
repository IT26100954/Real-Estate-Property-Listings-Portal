package com.realestate.model;

import java.util.Map;

public abstract class Review extends Entity {
  protected Review(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getReviewerId() {
    return get("reviewerId");
  }

  public String getTargetId() {
    return get("targetId");
  }

  public String getReviewType() {
    return get("reviewType");
  }

  public String getRating() {
    return get("rating");
  }

  public String getComment() {
    return get("comment");
  }

  public String getReviewDate() {
    return get("reviewDate");
  }

  public abstract String displayReview();
}
