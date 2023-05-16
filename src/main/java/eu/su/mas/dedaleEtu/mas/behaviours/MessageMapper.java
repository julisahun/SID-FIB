package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.function.Consumer;

import org.json.JSONObject;

import jade.core.behaviours.Behaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.BehaviourUtils;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageMapper extends OneShotBehaviour {
  private final AbstractDedaleAgent agent;

  public MessageMapper(Agent a) {
    super(a);
    this.agent = (AbstractDedaleAgent) a;
  }

  private void updatePosition(String body) {
    JSONObject parsedJson = new JSONObject(body);
    ;
    try {
      // JSONObject data = parsedJson.getJSONObject("data");
      // String position = data.getString("position");
      String position = "12";

      String id = BehaviourUtils.uuid();
      Behaviour walk = new WalkTo(this.agent, position, getSituatedAgent().getMap(), id);
      BehaviourUtils.registerBehaviour(this.agent, walk, id);

      HashMap<Integer, Behaviour> responses = BehaviourUtils.exitMsgMapper(this.agent, "Ok", "Error");
      Behaviour action = new Composer(this.agent, walk, new ConditionalBehaviour(this.agent, id, responses));

      this.agent.addBehaviour(action);
    } catch (Exception e) {
      e.printStackTrace();
      this.agent.addBehaviour(
          new MessageSender(
              this.agent,
              ACLMessage.NOT_UNDERSTOOD,
              "Error parsing JSON message",
              new String[] { parsedJson.get("sender").toString() }));
      System.out.println("Error parsing JSON message");
    }
  }

  private void updateMap(String body) {
    System.out.println("Updating map");
  }

  private void pong(String body) {
    Behaviour pong = new MessageSender(this.agent, "pong");
    this.agent.addBehaviour(pong);
  }

  @Override
  public void action() {
    HashMap<String, Consumer<String>> actions = new HashMap<>();
    actions.put("position", this::updatePosition);
    actions.put("map", this::updateMap);
    actions.put("ping", this::pong);
    this.myAgent.addBehaviour(new Listener(this.myAgent, actions));
  }

  private SituatedAgent getSituatedAgent() {
    return (SituatedAgent) this.agent;
  }

}
