package com.realestate.model;

import java.util.Map;

public final class PartTimeAgent extends Agent {
  public PartTimeAgent(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateCommission(double price) {
    return price * number("commissionRate") / 100;
  }

  public String summary() {
    return "Part time • Commission " + getCommissionRate() + "%";
  }
}
