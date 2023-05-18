package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

public class SituatedListeningPlanBody extends BeliefGoalPlanBody {
  private boolean sent = false;

  @Override
  protected void execute() {
    if (!sent) {
      Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", "slave");
      sent = true;
    }
    ACLMessage msg = this.myAgent.receive();
    if (msg == null)
      return;

    if (msg.getPerformative() == ACLMessage.INFORM) {
      String sender = msg.getSender().getLocalName();
      String content = msg.getContent();
      if (sender.equals("slave") && content.equals("pong")) {
        Belief b = getBeliefBase().getBelief(IS_SLAVE_ALIVE);
        b.setValue(true);
        setEndState(Plan.EndState.SUCCESSFUL);
        return;
      }
    }
  }
}
