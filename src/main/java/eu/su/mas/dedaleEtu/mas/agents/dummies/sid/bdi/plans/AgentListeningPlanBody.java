package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

public class AgentListeningPlanBody extends AbstractPlanBody {

  @Override
  public void action() {
    PingAgentGoal goal = (PingAgentGoal) getGoal();
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", goal.getAgent());
    setEndState(Plan.EndState.SUCCESSFUL);
  }
}
