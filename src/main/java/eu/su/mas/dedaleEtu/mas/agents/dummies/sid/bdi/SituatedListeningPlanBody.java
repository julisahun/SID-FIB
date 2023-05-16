package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.BehaviourUtils;
import jade.lang.acl.ACLMessage;

public class SituatedListeningPlanBody extends BeliefGoalPlanBody {
  private boolean sent = false;
  @Override
  protected void execute() {
    if (!sent) {
      BehaviourUtils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", "Slave");
      sent = true;
    }
    ACLMessage msg = this.myAgent.receive();
    if (msg == null) {
      return;
    }
    if (msg.getPerformative() == ACLMessage.INFORM) {
      String sender = msg.getSender().getLocalName();
      String content = msg.getContent();
      if (sender.equals("Slave") && content.equals("pong")) {
        setEndState(Plan.EndState.SUCCESSFUL);
      }
    }
  }
}
