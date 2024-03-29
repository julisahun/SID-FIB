package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONObject;

import dataStructures.tuple.Couple;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.core.Agent;
import eu.su.mas.dedale.env.Location;
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
    try {
      JSONObject data = parsedJson.getJSONObject("data");
      String position = data.getString("position");
      Boolean rejectNode = getSituatedAgent().isAlreadyRejected(position);
      if (rejectNode) {
        Utils.sendMessage(this.agent, ACLMessage.REJECT_PROPOSAL, body, "master");
        return;
      }
      Utils.sendMessage(this.agent, ACLMessage.ACCEPT_PROPOSAL, body, "master");

      String id = Utils.uuid();
      Behaviour walk = new WalkTo(this.agent, position, getSituatedAgent().getMap(), id);
      Utils.registerBehaviour(this.agent, walk, id);

      HashMap<Integer, Runnable> responses = mapResponses();

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

  private HashMap<Integer, Runnable> mapResponses() {
    HashMap<Integer, Runnable> responses = new HashMap<>();
    responses.put(0, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "success");
      body.put("map", getSituatedAgent().stringifyNodes());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    responses.put(1, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "finished");
      body.put("map", getSituatedAgent().stringifyNodes());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    responses.put(2, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "error");
      body.put("map", getSituatedAgent().stringifyNodes());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    return responses;
  }

  private void updateMap(String body) {
    System.out.println("Updating map");
  }

  private void pong(String body) {
    String currentPosition = getSituatedAgent().getCurrentPosition().getLocationId();
    getSituatedAgent().addNode(currentPosition);
    for (Couple<Location, List<Couple<Observation, Integer>>> neighbor : getSituatedAgent().observe()) {
      String neighborId = neighbor.getLeft().getLocationId();
      if (currentPosition == neighborId)
        continue;
      getSituatedAgent().addNode(currentPosition, neighbor.getLeft().getLocationId(), neighbor.getRight());
    }
    JSONObject response = new JSONObject();
    response.put("status", "pong");
    response.put("map", getSituatedAgent().stringifyNodes());
    Behaviour pong = new MessageSender(this.agent, response.toString());
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
