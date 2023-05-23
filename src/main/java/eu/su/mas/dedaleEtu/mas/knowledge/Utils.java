package eu.su.mas.dedaleEtu.mas.knowledge;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents.BDIAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.agents.ExplorerAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.MessageSender;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.UUID;
import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

import bdi4jade.belief.Belief;
import bdi4jade.core.SingleCapabilityAgent;

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
    ExplorerAgent agent = (ExplorerAgent) a;
    agent.registerBehaviour(id, b, BehaviourStatus.NOT_ACTIVE);
    return id;
  }

  public static HashMap<Integer, Behaviour> exitMsgMapper(Agent a, String success, String error) {
    return new HashMap<Integer, Behaviour>() {
      {
        put(0, new MessageSender(a, success));
        put(1, new MessageSender(a, error));
      }
    };
  }

  public static void activateBehaviour(Agent a, String id) {
    ExplorerAgent agent = (ExplorerAgent) a;
    agent.updateStatus(id, BehaviourStatus.ACTIVE);
  }

  public static void finishBehaviour(Agent a, String id, Integer code) {
    ExplorerAgent agent = (ExplorerAgent) a;
    agent.updateStatus(id, code == 0 ? BehaviourStatus.SUCCEEDED : BehaviourStatus.FAILED, code);
  }

  public static Integer getStatusCode(Agent a, String id) {
    ExplorerAgent agent = (ExplorerAgent) a;
    return agent.getStatus(id).getRight();
  }

  public static void sendMessage(Agent a, int performative, String content, String to) {
    ACLMessage msg = new ACLMessage(performative);
    msg.setProtocol("json");
    msg.setSender(a.getAID());
    msg.addReceiver(new AID(to, AID.ISLOCALNAME));
    msg.setContent(content);
    a.send(msg);
    if (to.equals("slave"))
      ((BDIAgent) a).addMessage(msg);
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

  // public static void addProperty(OntModel ont, String from, String property,
  // String to) {
  // Individual fromEntity = ont.getIndividual(BASE_URI + from);
  // Property nameProperty = ont.getProperty(BASE_URI + property);
  // fromEntity.addProperty(nameProperty, to);
  // }

  // public static void addProperty(Agent a, String from, String property, String
  // to) {
  // OntModel ont = getOntology(a);
  // addProperty(ont, from, property, to);
  // }

  // public static void addRelation(OntModel ont, String from, String property,
  // String to) {
  // Individual fromEntity = ont.getIndividual(BASE_URI + from);
  // Property nameProperty = ont.getProperty(BASE_URI + property);
  // Individual toEntity = ont.getIndividual(BASE_URI + to);
  // fromEntity.addProperty(nameProperty, toEntity);
  // }

  // public static void addRelation(Agent a, String from, String property, String
  // to) {
  // OntModel ont = getOntology(a);
  // addRelation(ont, from, property, to);
  // }

  // public static void addIndividual(OntModel ont, String className, String
  // instance) {
  // ont.createIndividual(BASE_URI + instance, ont.getOntClass(BASE_URI +
  // className));
  // }

  // public static void addIndividual(Agent a, String className, String instance)
  // {
  // OntModel ont = getOntology(a);
  // addIndividual(ont, className, instance);
  // }

  public static OntModel getOntology(Agent a) {
    SingleCapabilityAgent agent = (SingleCapabilityAgent) a;
    Belief b = agent.getCapability().getBeliefBase().getBelief(ONTOLOGY);
    Model model = (Model) b.getValue();
    return (OntModel) model;
  }
}
