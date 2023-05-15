package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.BehaviourUtils;
import jade.lang.acl.ACLMessage;

public class SituatedListeningPlanBody extends BeliefGoalPlanBody {
  @Override
  protected void execute() {
    BehaviourUtils.sendMessage(myAgent, ACLMessage.INFORM, "ping", "slave");
    System.out.println("Sent ping");
    ACLMessage msg = this.myAgent.receive();
    if (msg == null) {
      block();
      return;
    }
    if (msg.getPerformative() == ACLMessage.INFORM) {
      String sender = msg.getSender().getLocalName();
      String content = msg.getContent();
      if (sender == "slave" && content == "pong") {
        setEndState(Plan.EndState.SUCCESSFUL);
      }
    }
  }
}
