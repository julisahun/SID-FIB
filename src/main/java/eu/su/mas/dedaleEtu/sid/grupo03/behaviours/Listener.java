package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.json.JSONObject;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Message;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;

public class Listener extends CyclicBehaviour {
  private HashMap<String, Consumer<String>> actions;

  public Listener(Agent agent, HashMap<String, Consumer<String>> actions) {
    super(agent);
    this.actions = actions;
  }

  private Couple<String, String> getKeyValue(Message msg) {
    final Boolean isPolMarcetOntology = msg.ontology.equals("polydama-mapstate") &&
        msg.protocol.equals("SHARE-ONTO");
    if (isPolMarcetOntology) {
      JSONObject jsonContent = new JSONObject();
      jsonContent.put("ontology", msg.content);
      return new Couple("ontology", jsonContent.toString());
    }
    String[] contentArray = msg.content.split(":");
    String key = contentArray[0];
    String message = String.join(":", Arrays.copyOfRange(contentArray, 1,
        contentArray.length));
    return new Couple(key, message);
  }

  private Couple<String, JSONObject> mapMessage(Message msg) {
    Couple<String, String> keyValue = getKeyValue(msg);
    try {
      JSONObject content = new JSONObject(keyValue.getRight());
      JSONObject body = new JSONObject();
      body.put("performative", msg.performative);
      body.put("protocol", msg.protocol);
      body.put("sender", msg.sender);
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
    try {
      Message message = Utils.messageMiddleware(this.myAgent, msg);
      Couple<String, JSONObject> mappedMessage = mapMessage(message);
      String key = mappedMessage.getLeft();
      JSONObject body = mappedMessage.getRight();
      if (actions.containsKey(key))
        this.actions.get(key).accept(body.toString());
    } catch (Exception e) {
      System.out.println("Message error " + e.getMessage() + " agent: " + this.myAgent.getLocalName());
      e.printStackTrace();
    }

  }
}