package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

public class SituatedListeningPlanBody extends BeliefGoalPlanBody {

  @Override
  protected void execute() {
    ACLMessage msg = this.myAgent.receive();
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", "slave");
    if (msg == null) {
      block();
      return;
    }
    if (msg.getPerformative() == ACLMessage.INFORM) {
      String sender = msg.getSender().getLocalName();
      String content = msg.getContent();
      if (sender.equals("slave") && content.equals("pong")) {
        setEndState(Plan.EndState.SUCCESSFUL);
        return;
      }
    }
  }
}
