package com.realestate.dao;
import com.realestate.model.Appointment;
public final class AppointmentDAO extends SqlDAO<Appointment> {
  public AppointmentDAO() { super("appointments"); }
}
