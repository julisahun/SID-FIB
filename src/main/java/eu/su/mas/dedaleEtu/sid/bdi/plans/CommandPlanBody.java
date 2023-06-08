package eu.su.mas.dedaleEtu.sid.bdi.plans;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.sid.bdi.agents.BDIAgent03;
import eu.su.mas.dedaleEtu.sid.bdi.goals.CommandGoal;
import eu.su.mas.dedaleEtu.sid.core.Utils;
import jade.lang.acl.ACLMessage;

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
    String to = ((BDIAgent03) myAgent).situatedName;
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(), to);
  }
}
