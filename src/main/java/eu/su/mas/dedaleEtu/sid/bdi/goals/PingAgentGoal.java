package eu.su.mas.dedaleEtu.sid.bdi.goals;

import bdi4jade.goal.Goal;

public class PingAgentGoal implements Goal {
  private String agent;

  public PingAgentGoal(String agent) {
    this.agent = agent;
  }

  public String getAgent() {
    return agent;
  }

  public void setAgent(String name) {
    agent = name;
  }
}
