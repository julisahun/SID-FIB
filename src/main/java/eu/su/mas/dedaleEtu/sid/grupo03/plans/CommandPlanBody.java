package eu.su.mas.dedaleEtu.sid.grupo03.plans;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.sid.grupo03.BDIAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import eu.su.mas.dedaleEtu.sid.grupo03.goals.CommandGoal;
import jade.lang.acl.ACLMessage;

import java.util.regex.Pattern;

import org.json.JSONObject;

public class CommandPlanBody extends AbstractPlanBody {

  @Override
  public void action() {
    CommandGoal goal = (CommandGoal) getGoal();
    String command = goal.getCommand();
    sendCommand(command);
    setEndState(Plan.EndState.SUCCESSFUL);
  }

  private void sendCommand(String command) {
    if (isNumeric(command)) {
      sendPositionCommand(command);
    } else {
      sendMessageCommand(command);
    }
  }

  private Boolean isNumeric(String s) {
    Pattern pattern = Pattern.compile("\\d+");
    return pattern.matcher(s).matches();

  }

  private void sendPositionCommand(String position) {
    JSONObject body = new JSONObject();
    body.put("position", position);
    String to = ((BDIAgent03) myAgent).situatedName;
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(), to);
  }

  private void sendMessageCommand(String message) {
    JSONObject body = new JSONObject();
    body.put(message, 0);
    String to = ((BDIAgent03) myAgent).situatedName;
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, message + ":" + body.toString(), to);
  }
}
