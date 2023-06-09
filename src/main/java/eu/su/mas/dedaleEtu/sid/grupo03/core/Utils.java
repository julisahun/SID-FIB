package eu.su.mas.dedaleEtu.sid.grupo03.core;

import eu.su.mas.dedaleEtu.sid.grupo03.BDIAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.behaviours.MessageSender;
import eu.su.mas.dedaleEtu.sid.grupo03.core.MapaModel.NodeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.net.URL;

import static eu.su.mas.dedaleEtu.sid.grupo03.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONObject;

import bdi4jade.belief.Belief;
import bdi4jade.core.SingleCapabilityAgent;
import dataStructures.tuple.Couple;

public class Utils {
  public static enum BehaviourStatus {
    NOT_ACTIVE,
    ACTIVE,
    SUCCEEDED,
    FAILED
  }

  private final static String FILE_NAME = "mapa";

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

  public static MapaModel loadOntology() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
    OntDocumentManager dm = model.getDocumentManager();
    URL fileAsResource = Utils.class.getClassLoader().getResource(FILE_NAME + ".owl");
    dm.addAltEntry(FILE_NAME, fileAsResource.toString());
    model.read(FILE_NAME);
    return new MapaModel(model);
  }

  public static void saveOntology(Model ont) {
    try {
      System.out.println("Saving ontology..." + ont.isClosed());
      if (!ont.isClosed()) {
        String sep = File.separator;
        Path resourcePath = Paths.get(Utils.class.getResource(sep).getPath());
        ont.write(new FileOutputStream(resourcePath + sep + FILE_NAME +
            "-modified.owl", false));
        ont.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static HashSet<String> getNodesWith(Model model, String query) {
    HashSet<String> nodes = new HashSet<String>();
    String[] params = query.split(" ");
    String baseQuery = "PREFIX mapa: <http://mapa#> SELECT ?x WHERE { ?x mapa:" + params[0] + " ?" + params[0]
        + " . FILTER(?" + params[0] + " " + params[1] + " " + params[2] + ") }";
    QueryExecution qexec = QueryExecutionFactory.create(baseQuery, model);
    try {
      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        String result = soln.get("?x").toString();
        String cleanResult = result.split("_")[1];
        nodes.add(cleanResult);
      }
    } finally {
      qexec.close();
    }
    return nodes;
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

  public static void updateMap(Map patchUpdate, MapaModel ontology) {
    for (String node : patchUpdate.keySet()) {
      Node newNode = patchUpdate.get(node);
      ontology.addNode(node,
          newNode.getStatus() == Node.Status.OPEN ? NodeType.Open : NodeType.Closed);
      for (String neighbor : newNode.getNeighbors()) {
        ontology.addAdjancency(node, neighbor);
      }
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

  public static String findAgent(Agent a, String name) {
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    template.addServices(templateSd);
    try {
      DFAgentDescription[] results = DFService.search(a, template);
      if (results.length > 0) {
        for (DFAgentDescription dfd : results) {
          String result = dfd.getName().getLocalName();
          System.out.println("Found agent " + result + " with name " + name);
          if (result.contains(name))
            return result;
        }
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static OntModel getOntology(Agent a) {
    SingleCapabilityAgent agent = (SingleCapabilityAgent) a;
    Belief b = agent.getCapability().getBeliefBase().getBelief(ONTOLOGY);
    Model model = (Model) b.getValue();
    return (OntModel) model;
  }

  public static Message messageMiddleware(Agent a, ACLMessage msg) {
    final String receiver = a.getLocalName();
    Message message = new Message(msg, receiver);
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
}
