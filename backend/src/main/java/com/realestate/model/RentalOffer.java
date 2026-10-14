package com.realestate.model;

import java.util.Map;

public final class RentalOffer extends Offer {
  public RentalOffer(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateTotalPayable() {
    return number("offerAmount") * number("durationMonths");
  }

  public String summary() {
    return "Rental contract total LKR " + calculateTotalPayable();
  }
}
