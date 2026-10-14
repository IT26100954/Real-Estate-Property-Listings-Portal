package com.realestate.model;

import java.util.Map;

public abstract class Admin extends User {
  protected Admin(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getAdminLevel() {
    return get("adminLevel");
  }

  public abstract String getPermissions();
}
