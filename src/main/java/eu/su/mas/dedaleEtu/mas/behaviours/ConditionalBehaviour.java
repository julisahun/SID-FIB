package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

public class ConditionalBehaviour extends OneShotBehaviour {

  private HashMap<Integer, Behaviour> behaviours;
  private String dependant;

  public ConditionalBehaviour(Agent a, String id, HashMap<Integer, Behaviour> behaviours) {
    super(a);
    this.dependant = id;
    this.behaviours = behaviours;
  }

  @Override
  public void action() {
    SituatedAgent agent = (SituatedAgent) this.myAgent;
    Integer status = agent.getStatus(dependant).getRight();
    if (behaviours.containsKey(status)) {
      agent.addBehaviour(behaviours.get(status));
    }
  }

}
