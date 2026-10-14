package com.realestate.model;

import java.util.Map;

public abstract class Appointment extends Entity {
  protected Appointment(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getPropertyId() {
    return get("propertyId");
  }

  public String getBuyerId() {
    return get("buyerId");
  }

  public String getAgentId() {
    return get("agentId");
  }

  public String getViewDate() {
    return get("viewDate");
  }

  public String getViewTime() {
    return get("viewTime");
  }

  public String getViewType() {
    return get("viewType");
  }

  public String getStatus() {
    return get("status");
  }

  public String getNote() {
    return get("note");
  }

  public abstract String viewingInstructions();
}
