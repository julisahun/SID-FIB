package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import bdi4jade.belief.Belief;
import bdi4jade.goal.Goal;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents.BDIAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.CommandGoal;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import org.json.JSONObject;

public class CommandPlanBody extends AbstractPlanBody {

  @Override
  public void action() {
    CommandGoal goal = (CommandGoal) getGoal();
    String command = goal.getCommand();
    sendCommand(command);
    setEndState(Plan.EndState.SUCCESSFUL);
  }

  private void sendCommand(String position) {
    JSONObject body = new JSONObject();
    body.put("position", position);
    String to = BDIAgent.situatedName;
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(), to);
  }
}
