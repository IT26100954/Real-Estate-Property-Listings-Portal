package com.realestate.model;

import java.util.Map;

public final class FullTimeAgent extends Agent {
  public FullTimeAgent(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public double calculateCommission(double price) {
    return price * number("commissionRate") / 100 + 10000;
  }

  public String summary() {
    return "Full time • Commission " + getCommissionRate() + "% + LKR 10,000 bonus";
  }
}
