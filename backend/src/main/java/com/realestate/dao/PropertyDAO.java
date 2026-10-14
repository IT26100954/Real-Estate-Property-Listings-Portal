package com.realestate.dao;
import com.realestate.model.Property;
public final class PropertyDAO extends SqlDAO<Property> {
  public PropertyDAO() { super("properties"); }
}
