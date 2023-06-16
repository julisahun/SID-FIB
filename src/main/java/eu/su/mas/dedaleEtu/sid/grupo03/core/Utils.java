package eu.su.mas.dedaleEtu.sid.grupo03.core;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.sid.grupo03.BDIAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.MapaModel.NodeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static eu.su.mas.dedaleEtu.sid.grupo03.core.Constants.*;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.json.JSONObject;

import dataStructures.tuple.Couple;

public class Utils {
  public static enum BehaviourStatus {
    NOT_ACTIVE,
    ACTIVE,
    SUCCEEDED,
    FAILED
  }

  public static String uuid() {
    return UUID.randomUUID().toString();
  }

  public static String registerBehaviour(Agent a, Behaviour b, String id) {
    SituatedAgent03 agent = (SituatedAgent03) a;
    agent.registerBehaviour(id, b);
    return id;
  }

  public static void finishBehaviour(Agent a, String id, Integer code) {
    SituatedAgent03 agent = (SituatedAgent03) a;
    agent.updateStatus(id, code);
  }

  public static void sendMessage(Agent a, int performative, String content, String to) {
    ACLMessage msg = new ACLMessage(performative);
    msg.setProtocol("json");
    msg.setSender(a.getAID());
    msg.addReceiver(new AID(to, AID.ISLOCALNAME));
    msg.setContent(content);
    a.send(msg);
    if (to.equals("slave"))
      ((BDIAgent03) a).addMessage(msg);
  }

  public static String getLeastVisitedNode(Model model) {
    String query = "PREFIX mapa: <http://mapa#> SELECT ?x WHERE { ?x mapa:timesVisited ?timesVisited . FILTER(?timesVisited > 0) } ORDER BY ?timesVisited LIMIT 1";
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    try {
      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        String result = soln.get("?x").toString();
        String cleanResult = result.split("_")[1];
        return cleanResult;
      }
    } finally {
      qexec.close();
    }
    return null;
  }

  public static void updateMap(Map patchUpdate, MapaModel ontology, HashMap<String, Integer> rejectedNodes) {
    for (String node : patchUpdate.keySet()) {

      final Node.Status status = patchUpdate.get(node).getStatus();
      Node newNode = patchUpdate.get(node);
      ontology.addNode(node,
          status == Node.Status.OPEN ? NodeType.Open : NodeType.Closed);
      for (String neighbor : newNode.getNeighbors()) {
        ontology.addAdjancency(node, neighbor);
      }
      if (status == Node.Status.OPEN)
        // only update observations if node is closed
        continue;
      if (rejectedNodes.containsKey(node))
        rejectedNodes.remove(node);
      long diamondAmount = 0;
      long goldAmount = 0;
      long lockpickLevel = 0;
      long strength = 0;
      for (Couple<String, Integer> observation : newNode.getObservations()) {
        switch (observation.getLeft()) {
          case "Diamond":
            diamondAmount = observation.getRight();
            break;
          case "Gold":
            goldAmount = observation.getRight();
            break;
          case "LockPicking":
            lockpickLevel = observation.getRight();
            break;
          case "Strength":
            strength = observation.getRight();
            break;
          default:
            break;
        }
      }
      ontology.addNodeInfo(node, newNode.getTimesVisited(), goldAmount, diamondAmount, lockpickLevel, strength);
    }
  }

  public static HashSet<String> getTankers(Agent a) {
    HashSet<String> tankers = new HashSet<String>();
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("agentTanker");
    template.addServices(templateSd);
    try {
      DFAgentDescription[] results = DFService.search(a, template);
      if (results.length > 0) {
        for (DFAgentDescription dfd : results) {
          String result = dfd.getName().getLocalName();
          tankers.add(result);
        }
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    return tankers;
  }

  public static String findAgent(Agent a, String name) {
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    template.addServices(templateSd);
    try {
      DFAgentDescription[] results = DFService.search(a, template);
      if (results.length > 0) {
        for (DFAgentDescription dfd : results) {
          String result = dfd.getName().getLocalName();
          if (result.contains(name))
            return result;
        }
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Message messageMiddleware(Agent a, ACLMessage msg) {
    final String receiver = a.getLocalName();
    Message message = new Message(msg, receiver);
    if (IS_DEV)
      return message;
    if (receiver.equals(MASTER_NAME)) {
      if (!message.sender.equals(SITUATED_NAME)) {
        throw new RuntimeException("Message sent to master from " + message.sender);
      }
    }
    if (receiver.equals(SITUATED_NAME)) {
      if (!message.sender.equals(MASTER_NAME)
          && (!message.ontology.equals(ONTOLOGY_NAME) || !message.protocol.equals(ONTOLOGY_PROTOCOL))) {
        throw new RuntimeException("Message sent to situated from " + message.sender);
      }
    }
    return message;
  }

  public static JSONObject getBackPackFreeSpace(SituatedAgent03 agent) {
    JSONObject resources = new JSONObject();
    for (Couple<Observation, Integer> resource : agent.getBackPackFreeSpace()) {
      resources.put(resource.getLeft().getName().toLowerCase(), resource.getRight());
    }
    return resources;
  }
}
