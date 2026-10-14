package com.realestate.model;

import java.util.Map;

public final class SellerUser extends User {
  public SellerUser(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPermissions() {
    return "Manage own properties and review offers";
  }

  public String summary() {
    return getPermissions();
  }
}
