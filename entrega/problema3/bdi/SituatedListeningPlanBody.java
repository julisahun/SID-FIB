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
    Utils.sendMessage(myAgent, ACLMessage.REQUEST, "ping", "slave");
    setEndState(Plan.EndState.SUCCESSFUL);
  }
}
