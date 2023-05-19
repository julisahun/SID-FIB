package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import dataStructures.tuple.Couple;
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
      JSONObject body = new JSONObject(content);
      if (sender.equals("slave")) {
        HashMap map = new HashMap<>();
        for (String key : body.keySet()) {
          HashSet<String> neighbors = new HashSet<>();

          for (Object neighbor : body.getJSONObject(key).getJSONArray("neighbors")) {
            neighbors.add((String) neighbor);
          }
          Boolean status = neighbors.size() > 0;
          map.put(key, new Couple<Boolean, HashSet<String>>(status, neighbors));
        }
        Belief mapBelief = getBeliefBase().getBelief(MAP);
        mapBelief.setValue(map);
        Belief b = getBeliefBase().getBelief(IS_SLAVE_ALIVE);
        b.setValue(true);
        setEndState(Plan.EndState.SUCCESSFUL);
        return;
      }
    }
  }
}
