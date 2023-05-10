package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public class Listener extends CyclicBehaviour {
  private HashMap<String, Consumer<String>> actions;

  public Listener(Agent agent, HashMap<String, Consumer<String>> actions) {
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
    if (msg.getPerformative() == ACLMessage.REQUEST) {
      String content = msg.getContent();
      // String[] contentArray = content.split(":");
      // String key = contentArray[0];
      // String body = String.join(":", Arrays.copyOfRange(contentArray, 1,
      // contentArray.length));
      this.actions.get("position").accept("");
      // if (actions.containsKey(key))
      // this.actions.get(key).accept(body);
      // else
      // ((SituatedAgent) this.myAgent).addMesssage(key, body);
    }
  }
}