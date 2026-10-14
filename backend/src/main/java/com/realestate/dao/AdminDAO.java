package com.realestate.dao;
import com.realestate.model.Admin;
public final class AdminDAO extends SqlDAO<Admin> {
  public AdminDAO() { super("admins"); }
}
