package com.realestate.model;

import java.util.Map;

public abstract class Offer extends Entity {
  protected Offer(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPropertyId() {
    return get("propertyId");
  }

  public String getBuyerId() {
    return get("buyerId");
  }

  public String getOfferType() {
    return get("offerType");
  }

  public String getOfferAmount() {
    return get("offerAmount");
  }

  public String getDurationMonths() {
    return get("durationMonths");
  }

  public String getOfferDate() {
    return get("offerDate");
  }

  public String getStatus() {
    return get("status");
  }

  public abstract double calculateTotalPayable();
}
