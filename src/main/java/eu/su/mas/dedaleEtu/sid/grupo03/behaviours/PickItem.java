package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class PickItem extends SimpleBehaviour {
  private int amountPicked = 0;
  PickItem(Agent a) {
    super(a);
  }

  public void action() {
    this.amountPicked = ((SituatedAgent03) this.myAgent).pick();
  }

  public boolean done() {
    return true;
  }

  public int onEnd() {
    return this.amountPicked;
  }
}
