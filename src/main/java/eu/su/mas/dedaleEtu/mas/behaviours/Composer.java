package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;

public class Composer extends SequentialBehaviour {
  private List<Behaviour> behaviours;

  public Composer(List<Behaviour> behaviours) {
    super();
    this.behaviours = behaviours;
    this.start();
  }

  public Composer(Behaviour main, Behaviour callback) {
    super();
    this.behaviours = List.of(main, callback);
    this.start();
  }

  public void start() {
    for (Behaviour behaviour : this.behaviours) {
      this.addSubBehaviour(behaviour);
    }
  }
}
