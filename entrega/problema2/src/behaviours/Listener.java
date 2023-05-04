package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.function.Consumer;

public class Listener extends CyclicBehaviour {
  private HashMap<String, Consumer> actions;

  public Listener(Agent agent, HashMap<String, Consumer> actions) {
    super(agent);
    this.actions = actions;
  }

  @Override
  public void action() {
    ACLMessage msg = this.myAgent.receive();
    if (msg == null) {
      block();
      return;
    }

    if (msg.getPerformative() == ACLMessage.INFORM) {
      String content = msg.getContent();
      String header = content.split(":")[0];
      String body = content.split(":")[1];
      this.actions.get(header).accept(body);
    }
  }
}