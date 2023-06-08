package eu.su.mas.dedaleEtu.sid.situated.behaviours;

import java.util.HashMap;

import eu.su.mas.dedaleEtu.sid.situated.agents.SituatedAgent03;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class ConditionalBehaviour extends OneShotBehaviour {

  private HashMap<Integer, Runnable> actions;
  private String dependant;

  public ConditionalBehaviour(Agent a, String dependant, HashMap<Integer, Runnable> actions) {
    super(a);
    this.dependant = dependant;
    this.actions = actions;
  }

  @Override
  public void action() {
    SituatedAgent03 agent = (SituatedAgent03) this.myAgent;
    Integer status = agent.getStatus(dependant).getRight();
    if (actions.containsKey(status)) {
      actions.get(status).run();
    }
  }

}
