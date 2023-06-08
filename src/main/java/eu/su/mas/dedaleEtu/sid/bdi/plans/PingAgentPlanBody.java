package eu.su.mas.dedaleEtu.sid.bdi.plans;

import static eu.su.mas.dedaleEtu.sid.core.Constants.*;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.sid.bdi.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.sid.core.Utils;
import jade.lang.acl.ACLMessage;

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
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping:{}", agentToPing);
    getCapability().getBeliefBase().updateBelief(SITUATED_PINGED, true);
    setEndState(Plan.EndState.SUCCESSFUL);
  }
}
