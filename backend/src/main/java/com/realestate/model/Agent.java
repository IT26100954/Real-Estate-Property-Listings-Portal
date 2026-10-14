package com.realestate.model;

import java.util.Map;

public abstract class Agent extends Entity {
  protected Agent(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String getFullName() {
    return get("fullName");
  }

  public String getEmail() {
    return get("email");
  }

  public String getPhone() {
    return get("phone");
  }

  public String getLicenseNo() {
    return get("licenseNo");
  }

  public String getCity() {
    return get("city");
  }

  public String getExperienceYears() {
    return get("experienceYears");
  }

  public String getAgentType() {
    return get("agentType");
  }

  public String getCommissionRate() {
    return get("commissionRate");
  }

  public abstract double calculateCommission(double price);
}
