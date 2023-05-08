package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;

public class TestBehaviour extends OneShotBehaviour {
  public TestBehaviour() {
    super();
  }

  @Override
  public void action() {
    System.out.println("TestBehaviour");
  }

}
