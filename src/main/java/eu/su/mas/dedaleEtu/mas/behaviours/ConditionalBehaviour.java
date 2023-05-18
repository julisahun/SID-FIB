package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
    SituatedAgent agent = (SituatedAgent) this.myAgent;
    Integer status = agent.getStatus(dependant).getRight();
    if (actions.containsKey(status)) {
      actions.get(status).run();
    }
  }

}
