package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import java.util.List;

import org.apache.jena.sparql.function.library.leviathan.cot;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;

import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class PickItem extends SimpleBehaviour {
  private int amountPicked = 0;
  private String id;

  PickItem(Agent a, String id) {
    super(a);
    this.id = id;
  }

  public void action() {
    SituatedAgent03 situated = (SituatedAgent03) this.myAgent;
    for (Couple<Location, List<Couple<Observation, Integer>>> obs : situated.observe()) {
      if (!obs.getLeft().equals(situated.getCurrentPosition()))
        continue;
      for (Couple<Observation, Integer> ob : obs.getRight()) {
        if (ob.getLeft().getName().equals("LockIsOpen")) {
          if (ob.getRight() == 0) {
            Boolean lockOpen = situated.openLock(ob.getLeft());
            if (lockOpen) {
              this.amountPicked = situated.pick();
              return;
            }
          }
          return;
        }
      }
    }
  }

  public boolean done() {
    return true;
  }

  public int onEnd() {
    int status;
    if (this.amountPicked > 0)
      status = 0;
    status = 1;

    Utils.finishBehaviour(this.myAgent, this.id, status);
    return status;
  }
}
