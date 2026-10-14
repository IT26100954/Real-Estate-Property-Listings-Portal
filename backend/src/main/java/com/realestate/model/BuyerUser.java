package com.realestate.model;

import java.util.Map;

public final class BuyerUser extends User {
  public BuyerUser(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPermissions() {
    return "Browse, book viewings, make offers and review";
  }

  public String summary() {
    return getPermissions();
  }
}
