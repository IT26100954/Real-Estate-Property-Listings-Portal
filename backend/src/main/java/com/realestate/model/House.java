package com.realestate.model;

import java.util.Map;

public final class House extends Property {
  public House(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateListingFee() {
    return number("price") * 0.01;
  }

  public String summary() {
    return "House • " + getExtraValue() + " floors • Listing fee LKR " + calculateListingFee();
  }
}
