package com.realestate.model;

import java.util.Map;

public final class Apartment extends Property {
  public Apartment(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateListingFee() {
    return number("price") * 0.0075;
  }

  public String summary() {
    return "Apartment • Floor " + getExtraValue() + " • Listing fee LKR " + calculateListingFee();
  }
}
