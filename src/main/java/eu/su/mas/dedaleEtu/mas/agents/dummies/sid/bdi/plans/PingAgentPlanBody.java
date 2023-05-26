package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;
import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

public class PingAgentPlanBody extends AbstractPlanBody {

  @Override
  public void action() {
    PingAgentGoal goal = (PingAgentGoal) getGoal();
    String agentToPing = goal.getAgent();
    agentToPing = Utils.findAgent(myAgent, agentToPing);
    if (agentToPing == null) {
      setEndState(Plan.EndState.FAILED);
      return;
    }
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", agentToPing);
    getCapability().getBeliefBase().updateBelief(SITUATED_PINGED, true);
    setEndState(Plan.EndState.SUCCESSFUL);
  }
}
