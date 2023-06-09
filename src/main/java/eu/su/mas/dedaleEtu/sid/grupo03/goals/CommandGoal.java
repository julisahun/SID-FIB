package eu.su.mas.dedaleEtu.sid.grupo03.goals;

import bdi4jade.goal.Goal;

public class CommandGoal implements Goal {

  private String command;

  public CommandGoal(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
}
