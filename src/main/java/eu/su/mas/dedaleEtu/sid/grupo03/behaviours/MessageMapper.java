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
import eu.su.mas.dedaleEtu.sid.grupo03.core.Map;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Node;
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

  private String observeCurrentNode() {
    Map map = new Map();
    final String currentPosition = this.agent.getCurrentPosition().toString();
    for (Couple<Location, List<Couple<Observation, Integer>>> neighbor : this.agent.observe()) {
      if (!neighbor.getLeft().toString().equals(currentPosition))
        continue;
      Node n = new Node(currentPosition, Node.Status.CLOSED, neighbor.getRight());
      map.put(currentPosition, n);
      String resource = null;
      Integer amount = 0;

      for (Couple<Observation, Integer> observation : neighbor.getRight()) {
        if (observation.getLeft().getName().equals("Diamond") || observation.getLeft().getName().equals("Gold")) {
          resource = observation.getLeft().getName().toLowerCase();
          amount = observation.getRight();
        }
      }
      this.agent.updateObs(currentPosition, new Couple<String, Integer>(resource, amount));
    }
    return map.stringifyNodes();
  }

  private JSONObject getBackPackFreeSpace() {
    JSONObject resources = new JSONObject();
    for (Couple<Observation, Integer> resource : this.agent.getBackPackFreeSpace()) {
      resources.put(resource.getLeft().getName().toLowerCase(), resource.getRight());
    }
    return resources;
  }

  private void pickup(String body) {
    String id = Utils.uuid();
    Behaviour pickItem = new PickItem(this.agent, id);
    Utils.registerBehaviour(this.agent, pickItem, id);

    HashMap<Integer, Runnable> responses = new HashMap<>();
    responses.put(0, () -> {
      JSONObject res = new JSONObject();
      res.put("status", "success");
      res.put("command", "collect");
      res.put("resourcesCapacity", getBackPackFreeSpace());
      res.put("position", this.agent.getCurrentPosition().toString());
      res.put("map", this.observeCurrentNode());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, res.toString()));
    });
    responses.put(1, () -> {
      JSONObject res = new JSONObject();
      res.put("status", "error");
      res.put("command", "collect");
      res.put("resourcesCapacity", getBackPackFreeSpace());
      res.put("position", this.agent.getCurrentPosition().toString());
      res.put("map", this.observeCurrentNode());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, res.toString()));
    });
    Behaviour action = new Composer(this.agent, pickItem, new ConditionalBehaviour(this.agent, id, responses));

    this.agent.addBehaviour(action);
  }

  private void updatePosition(String body) {
    JSONObject parsedJson = new JSONObject(body);
    try {
      JSONObject data = parsedJson.getJSONObject("data");
      final String position = data.getString("position");
      if (position == "") {
        {
          JSONObject res = new JSONObject();
          res.put("status", "success");
          res.put("map", getSituatedAgent().getMap());
          res.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
          res.put("req", position);
          res.put("command", "move");
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
      JSONObject res = new JSONObject();
      res.put("status", "success");
      res.put("map", getSituatedAgent().getMap());
      res.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      res.put("req", position);
      res.put("command", "move");
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, res.toString()));
    });
    responses.put(1, () -> {
      JSONObject res = new JSONObject();
      res.put("status", "error");
      res.put("req", position);
      res.put("command", "move");
      res.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      res.put("map", getSituatedAgent().getMap());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, res.toString()));
    });
    responses.put(2, () -> {
      JSONObject res = new JSONObject();
      res.put("status", "finished");
      res.put("req", position);
      res.put("command", "move");
      res.put("position", getSituatedAgent().getCurrentPosition().getLocationId());
      res.put("map", getSituatedAgent().getMap());
      this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, res.toString()));
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
    this.agent.addBehaviour(new MessageSender(this.agent, ACLMessage.INFORM, data.toString()));
  }

  private void pong(String body) {
    String master = new JSONObject(body).getString("sender");
    this.agent.master = master;
    String currentPosition = getSituatedAgent().getCurrentPosition().getLocationId();
    getSituatedAgent().addNode(currentPosition);
    for (Couple<Location, List<Couple<Observation, Integer>>> neighbor : getSituatedAgent().observe()) {
      getSituatedAgent().addNode(currentPosition, neighbor.getLeft().getLocationId(), neighbor.getRight());
    }
    SituatedAgent03 myself = (SituatedAgent03) this.agent;
    JSONObject response = new JSONObject();
    response.put("status", "suceess");
    response.put("command", "pong");
    response.put("map", getSituatedAgent().getMap());
    response.put("agentType", myself.getType());
    response.put("position", currentPosition);
    response.put("resourcesCapacity", getBackPackFreeSpace());
    Behaviour pong = new MessageSender(this.agent, response.toString());
    this.agent.addBehaviour(pong);
  }

  @Override
  public void action() {
    HashMap<String, Consumer<String>> actions = new HashMap<>();
    actions.put("position", this::updatePosition);
    actions.put("collect", this::pickup);
    actions.put("map", this::updateMap);
    actions.put("ontology", this::updateOntology);
    actions.put("ping", this::pong);
    this.myAgent.addBehaviour(new Listener(this.myAgent, actions));
  }

  private SituatedAgent03 getSituatedAgent() {
    return (SituatedAgent03) this.agent;
  }
}
