package com.realestate.model;

import java.util.Map;

public final class VirtualViewing extends Appointment {
  public VirtualViewing(String id, Map<String, String> fields) {
    super(id, fields);
  }

  public String viewingInstructions() {
    return "Contact the assigned agent for your private video meeting link.";
  }

  public String summary() {
    return viewingInstructions();
  }
}
