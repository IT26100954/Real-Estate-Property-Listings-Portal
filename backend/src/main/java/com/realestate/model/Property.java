package com.realestate.model;

import java.util.Map;

public abstract class Property extends Entity {
  protected Property(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getTitle() {
    return get("title");
  }

  public String getPropertyType() {
    return get("propertyType");
  }

  public String getListingType() {
    return get("listingType");
  }

  public String getCity() {
    return get("city");
  }

  public String getAddress() {
    return get("address");
  }

  public String getPrice() {
    return get("price");
  }

  public String getBedrooms() {
    return get("bedrooms");
  }

  public String getBathrooms() {
    return get("bathrooms");
  }

  public String getAreaSqft() {
    return get("areaSqft");
  }

  public String getOwnerId() {
    return get("ownerId");
  }

  public String getAgentId() {
    return get("agentId");
  }

  public String getStatus() {
    return get("status");
  }

  public String getExtraValue() {
    return get("extraValue");
  }

  public abstract double calculateListingFee();
}
