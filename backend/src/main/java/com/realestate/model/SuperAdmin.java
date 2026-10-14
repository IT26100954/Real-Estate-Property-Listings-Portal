package com.realestate.model;

import java.util.Map;

public final class SuperAdmin extends Admin {
  public SuperAdmin(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPermissions() {
    return "All modules and administrator management";
  }

  public String summary() {
    return getPermissions();
  }
}
