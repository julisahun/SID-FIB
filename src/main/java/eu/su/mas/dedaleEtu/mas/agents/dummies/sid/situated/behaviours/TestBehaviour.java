package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.agents.ExplorerAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils.BehaviourStatus;;

public class TestBehaviour extends OneShotBehaviour {

  private String id;

  public TestBehaviour(Agent a, String id) {
    super(a);
    this.id = id;

  }

  @Override
  public void action() {
    Couple<BehaviourStatus, Integer> status = ((ExplorerAgent) this.myAgent).getStatus(id);
    System.out.println(status.getRight());
    System.out.println("TestBehaviour");
  }

}
