package com.realestate.model;

import java.util.Map;

public final class Land extends Property {
  public Land(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateListingFee() {
    return number("price") * 0.005;
  }

  public String summary() {
    return "Land • " + getExtraValue() + " perches • Listing fee LKR " + calculateListingFee();
  }
}
