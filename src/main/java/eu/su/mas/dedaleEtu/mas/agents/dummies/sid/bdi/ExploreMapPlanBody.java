package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

public class ExploreMapPlanBody extends BeliefGoalPlanBody {

  private boolean commandSent = false;
  private String commandedNode = "";
  private boolean commandAccepted = false;
  private HashSet<String> rejectedNodes = new HashSet<>();
  private HashMap map = new HashMap();

  @Override
  protected void execute() {
    if (!commandSent) {
      this.map = (HashMap) getBeliefBase().getBelief(MAP).getValue();
      this.commandedNode = getPotentialNode(map);
      command();
    } else {
      ACLMessage msg = this.myAgent.receive();
      if (msg == null)
        return;
      System.out.println("Received message: " + msg.getContent());
      if (msg.getPerformative() == ACLMessage.INFORM) {
        String sender = msg.getSender().getLocalName();
        String content = msg.getContent();
        JSONObject body = new JSONObject(content);
        if (sender.equals("slave")) {
          String status = body.getString("status");
          if (status == "success") {
            updateMap(body.getString("map"));
            return;
          } else if (status == "error") {
            this.rejectedNodes.add(this.commandedNode);
            return;
          } else if (status == "finished") {
            this.commandSent = false;
            this.commandAccepted = false;
            this.rejectedNodes = new HashSet<>();
            return;
          }
        }
      } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
        String sender = msg.getSender().getLocalName();
        if (sender.equals("slave") && !this.commandAccepted) {
          this.commandAccepted = true;
          return;
        }
      } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
        String sender = msg.getSender().getLocalName();
        if (sender.equals("slave")) {
          this.rejectedNodes.add(this.commandedNode);
          return;
        }
      }
    }

  }

  private void command() {
    JSONObject body = new JSONObject();
    body.put("position", this.commandedNode);
    System.out.println("Commanding node " + this.commandedNode);
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "position:" + body.toString(), "slave");
    this.commandSent = true;
  }

  private String getPotentialNode(HashMap map) {
    Integer node = 1;
    while (true) {
      String nodeString = node.toString();
      if (!map.containsKey(nodeString) && !this.rejectedNodes.contains(nodeString)) {
        return nodeString;
      }
      node++;
    }
  }

  private HashMap parseMap(String stringifiedMap) {
    JSONObject map = new JSONObject(stringifiedMap);
    HashMap<String, HashSet<String>> parsedMap = new HashMap<>();
    for (String node : map.keySet()) {
      HashSet<String> neighbors = new HashSet<>();
      for (Object neighbor : map.getJSONArray(node)) {
        neighbors.add(neighbor.toString());
      }
      parsedMap.put(node, neighbors);
    }
    return parsedMap;
  }

  private void updateMap(String stringifiedMap) {
    HashMap newMap = parseMap(stringifiedMap);
    this.map = newMap;
  }
}
