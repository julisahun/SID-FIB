package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashMap;
import java.util.UUID;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageSender;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class BehaviourUtils {
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
    SituatedAgent agent = (SituatedAgent) a;
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
    SituatedAgent agent = (SituatedAgent) a;
    agent.updateStatus(id, BehaviourStatus.ACTIVE);
  }

  public static void finishBehaviour(Agent a, String id, Integer code) {
    SituatedAgent agent = (SituatedAgent) a;
    agent.updateStatus(id, code == 0 ? BehaviourStatus.SUCCEEDED : BehaviourStatus.FAILED, code);
  }

  public static Integer getStatusCode(Agent a, String id) {
    SituatedAgent agent = (SituatedAgent) a;
    return agent.getStatus(id).getRight();
  }

  public static void sendMessage(Agent a, int performative, String content, String to) {
    ACLMessage msg = new ACLMessage(performative);
    msg.setProtocol("json");
    msg.setSender(a.getAID());
    msg.addReceiver(new AID(to, AID.ISLOCALNAME));
    msg.setContent(content);
    System.out.println("Sending message to " + to + ": " + content);
    a.send(msg);
  }
}
