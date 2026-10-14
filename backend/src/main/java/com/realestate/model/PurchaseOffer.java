package com.realestate.model;

import java.util.Map;

public final class PurchaseOffer extends Offer {
  public PurchaseOffer(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateTotalPayable() {
    return number("offerAmount");
  }

  public String summary() {
    return "Purchase total LKR " + calculateTotalPayable();
  }
}
