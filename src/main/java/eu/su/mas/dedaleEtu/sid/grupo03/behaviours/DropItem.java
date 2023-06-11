package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class DropItem extends SimpleBehaviour {

  private String tankerName;
  private Boolean dropped;

  DropItem(Agent a, String tankerName) {
    super(a);
    this.tankerName = tankerName;
  }

  public void action() {
    this.dropped = ((SituatedAgent03) this.myAgent).emptyMyBackPack(this.tankerName);
  }

  public boolean done() {
    return true;
  }

  public int onEnd() {
    if (this.dropped)
      return 0;
    return 1;
  }
}
