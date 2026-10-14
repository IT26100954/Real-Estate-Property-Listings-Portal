package com.realestate.model;

import java.util.Map;

public final class OnsiteViewing extends Appointment {
  public OnsiteViewing(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String viewingInstructions() {
    return "Meet at the property 10 minutes before your appointment.";
  }

  public String summary() {
    return viewingInstructions();
  }
}
