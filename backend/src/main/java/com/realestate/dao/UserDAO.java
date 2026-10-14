package com.realestate.dao;
import com.realestate.model.User;
public final class UserDAO extends SqlDAO<User> {
  public UserDAO() { super("users"); }
}
