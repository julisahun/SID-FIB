package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import bdi4jade.belief.Belief;
import bdi4jade.goal.Goal;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

public class CommandExplorerPlanBody extends AbstractPlanBody {

  private String commandedNode = "";
  private HashSet<String> rejectedNodes = new HashSet<>();
  private HashMap map = new HashMap();

  @Override
  public void action() {
    this.map = (HashMap) getBeliefBase().getBelief(MAP).getValue();
    this.commandedNode = getPotentialNode(map);
    command();
    Belief commandSent = getBeliefBase().getBelief(COMMAND_SENT);
    commandSent.setValue(true);
    setEndState(Plan.EndState.SUCCESSFUL);
  }

  private void command() {
    JSONObject body = new JSONObject();
    body.put("position", this.commandedNode);
    System.out.println("Commanding " + this.commandedNode);
    // Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(),
    // "explorer");
  }

  private String getPotentialNode(HashMap map) {
    for (Object key : map.keySet()) {
      String node = (String) key;
      Couple<Boolean, HashSet<String>> nodeInfo = (Couple<Boolean, HashSet<String>>) map.get(node);
      Boolean status = nodeInfo.getLeft();
      if (!status && !this.rejectedNodes.contains(node)) {
        return node;
      }
    }
    return "";
  }
}
