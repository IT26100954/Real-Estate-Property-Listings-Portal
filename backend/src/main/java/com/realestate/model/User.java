package com.realestate.model;

import java.util.Map;
/*common model for buyers , sellers and agents users.
inherits identity and fields from Entity */

public abstract class User extends Entity {

  //initialize the user with and ID and fields
  protected User(String id, Map<String, String> fields) {
    super(id, fields);
  }

  //return the account username
  public String getUsername() {
    return get("username");
  }

  //return the stored password value
  public String getPassword() {
    return get("password");
  }

  //return the account email address
  public String getEmail() {
    return get("email");
  }

  //return the account phone number
  public String getPhone() {
    return get("phone");
  }

  //return the account user type
  public String getUserType() {
    return get("userType");
  }

  public abstract String getPermissions();
}
