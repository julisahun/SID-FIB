package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import dataStructures.tuple.Couple;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.Agent;
import eu.su.mas.dedale.env.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageMapper extends OneShotBehaviour {
  private final SituatedAgent03 agent;

  public MessageMapper(Agent a) {
    super(a);
    this.agent = (SituatedAgent03) a;
  }

  private void updatePosition(String body) {
    JSONObject parsedJson = new JSONObject(body);
    try {
      JSONObject data = parsedJson.getJSONObject("data");
      String position = data.getString("position");
      if (position == "") {
        {
          JSONObject res = new JSONObject();
          res.put("status", "success");
          res.put("map", getSituatedAgent().stringifyNodes());
          res.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
          res.put("command", position);
          this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
        }
        return;
      }

      Boolean rejectNode = getSituatedAgent().isAlreadyRejected(position);
      if (rejectNode) {
        Utils.sendMessage(this.agent, ACLMessage.REJECT_PROPOSAL, body, this.agent.master);
        return;
      }
      Utils.sendMessage(this.agent, ACLMessage.ACCEPT_PROPOSAL, body, this.agent.master);

      String id = Utils.uuid();
      Behaviour walk = new WalkTo(this.agent, position, getSituatedAgent().getMapRepresentation(), id);
      Utils.registerBehaviour(this.agent, walk, id);

      HashMap<Integer, Runnable> responses = mapResponses(position);

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

  private HashMap<Integer, Runnable> mapResponses(String position) {
    HashMap<Integer, Runnable> responses = new HashMap<>();
    responses.put(0, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "success");
      body.put("map", getSituatedAgent().stringifyNodes());
      body.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      body.put("command", position);
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    responses.put(1, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "finished");
      body.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      body.put("command", position);
      body.put("map", getSituatedAgent().stringifyNodes());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    responses.put(2, () -> {
      JSONObject body = new JSONObject();
      body.put("status", "error");
      body.put("command", position);
      body.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      body.put("map", getSituatedAgent().stringifyNodes());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, body.toString()));
    });
    return responses;
  }

  private void updateMap(String body) {
    JSONObject parsedJson = new JSONObject(body);
    JSONObject data = parsedJson.getJSONObject("data");
    try {
      String ontology = data.getString("ontology");
      ((SituatedAgent03) this.agent).setOntology(ontology);
    } catch (Exception e) {
    }
    try {
      JSONObject mapJSON = data.getJSONObject("map");

      JSONArray openNodes = mapJSON.getJSONArray("openNodes");
      JSONArray closedNodes = mapJSON.getJSONArray("closedNodes");
      JSONArray edges = mapJSON.getJSONArray("edges");

      MapRepresentation map = new MapRepresentation();
      for (Object openNode : openNodes) {
        map.addNode(openNode.toString(), MapRepresentation.MapAttribute.open);
      }
      for (Object closedNode : closedNodes) {
        map.addNode(closedNode.toString(), MapRepresentation.MapAttribute.closed);
      }
      for (Object edge : edges) {
        JSONObject edgeJson = (JSONObject) edge;
        String from = edgeJson.getString("from");
        String to = edgeJson.getString("to");
        map.addEdge(from, to);
      }
      getSituatedAgent().setMap(map);
    } catch (Exception e) {
    }
  }

  private void updateOntology(String body) {
    JSONObject parsedJson = new JSONObject(body);
    JSONObject data = parsedJson.getJSONObject("data");
    System.out.println("Updating ontology " + this.agent.getLocalName());
    this.myAgent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, data.toString()));
  }

  private void pong(String body) {
    String master = new JSONObject(body).getString("sender");
    this.agent.master = master;
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
    response.put("agentType", ((SituatedAgent03) this.agent).getType());
    response.put("position", currentPosition);
    Behaviour pong = new MessageSender(this.agent, response.toString());
    this.agent.addBehaviour(pong);
  }

  @Override
  public void action() {
    HashMap<String, Consumer<String>> actions = new HashMap<>();
    actions.put("position", this::updatePosition);
    actions.put("map", this::updateMap);
    actions.put("ontology", this::updateOntology);
    actions.put("ping", this::pong);
    this.myAgent.addBehaviour(new Listener(this.myAgent, actions));
  }

  private SituatedAgent03 getSituatedAgent() {
    return (SituatedAgent03) this.agent;
  }
}
