package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.UUID;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

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
}
