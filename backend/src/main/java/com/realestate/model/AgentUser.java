package com.realestate.model;

import java.util.Map;

public final class AgentUser extends User {
  public AgentUser(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPermissions() {
    return "Manage assigned listings and viewing appointments";
  }

  public String summary() {
    return getPermissions();
  }
}
