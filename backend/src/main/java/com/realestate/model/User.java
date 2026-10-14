package com.realestate.model;

import java.util.Map;

public abstract class User extends Entity {
  protected User(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getUsername() {
    return get("username");
  }

  public String getPassword() {
    return get("password");
  }

  public String getEmail() {
    return get("email");
  }

  public String getPhone() {
    return get("phone");
  }

  public String getUserType() {
    return get("userType");
  }

  public abstract String getPermissions();
}
