package com.realestate.model;

import java.util.Map;

public final class ContentAdmin extends Admin {
  public ContentAdmin(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPermissions() {
    return "Manage property listings and moderate reviews";
  }

  public String summary() {
    return getPermissions();
  }
}
