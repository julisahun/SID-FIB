package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

public class ExploreMapPlanBody extends BeliefGoalPlanBody {

  private String commandedNode = "";
  private HashSet<String> rejectedNodes = new HashSet<>();
  private HashMap map = new HashMap();

  @Override
  protected void execute() {
    this.map = (HashMap) getBeliefBase().getBelief(MAP).getValue();
    this.commandedNode = getPotentialNode(map);
    command();
    setEndState(Plan.EndState.SUCCESSFUL);
  }

  private void command() {
    JSONObject body = new JSONObject();
    body.put("position", this.commandedNode);
    System.out.println("Commanding node " + this.commandedNode);
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(), "slave");
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
