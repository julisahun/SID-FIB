package eu.su.mas.dedaleEtu.sid.situated.behaviours;

import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

public class Composer extends SequentialBehaviour {
  private List<Behaviour> behaviours;

  public Composer(List<Behaviour> behaviours) {
    super();
    this.behaviours = behaviours;
    this.start();
  }

  public Composer(Agent a, Behaviour main, Behaviour callbacks) {
    super(a);
    this.addSubBehaviour(main);
    this.addSubBehaviour(callbacks);

  }

  public void start() {
    for (Behaviour behaviour : this.behaviours) {
      this.addSubBehaviour(behaviour);
    }
  }
}
