package com.realestate.dao;
import com.realestate.model.Agent;
public final class AgentDAO extends SqlDAO<Agent> {
  public AgentDAO() { super("agents"); }
}
