package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

import org.json.JSONObject;

import dataStructures.tuple.Couple;

public class Listener extends CyclicBehaviour {
  private HashMap<String, Consumer<String>> actions;

  public Listener(Agent agent, HashMap<String, Consumer<String>> actions) {
    super(agent);
    this.actions = actions;
  }

  private Couple<String, String> getKeyValue(ACLMessage msg) {
    String[] contentArray = msg.getContent().split(":");
    String key = contentArray[0];
    String message = String.join(":", Arrays.copyOfRange(contentArray, 1,
        contentArray.length));
    return new Couple(key, message);
  }

  private Couple<String, JSONObject> mapMessage(ACLMessage msg) {
    Couple<String, String> keyValue = getKeyValue(msg);
    try {
      JSONObject content = new JSONObject(keyValue.getRight());
      String protocol = msg.getProtocol();
      String sender = msg.getSender().getLocalName();
      JSONObject body = new JSONObject();
      body.put("protocol", protocol);
      body.put("sender", sender);
      body.put("data", content);

      return new Couple<String, JSONObject>(keyValue.getLeft(), body);
    } catch (Exception e) {
      return new Couple<String, JSONObject>(keyValue.getLeft(), new JSONObject("{}"));
    }
  }

  @Override
  public void action() {
    ACLMessage msg = this.myAgent.receive();
    if (msg == null) {
      block();
      return;
    }
    if (msg.getPerformative() == ACLMessage.REQUEST) {
      Couple<String, JSONObject> mappedMessage = mapMessage(msg);
      String key = mappedMessage.getLeft();
      JSONObject body = mappedMessage.getRight();
      if (actions.containsKey(key))
        this.actions.get(key).accept(body.toString());
      // else
      // ((SituatedAgent) this.myAgent).addMessage(key, body);
    }
  }
}