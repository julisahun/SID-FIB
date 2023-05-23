package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals;

import bdi4jade.goal.Goal;

public class PingAgentGoal implements Goal {
  private final String agent;

  public PingAgentGoal(String agent) {
    this.agent = agent;
  }

  public String getAgent() {
    return agent;
  }
}
