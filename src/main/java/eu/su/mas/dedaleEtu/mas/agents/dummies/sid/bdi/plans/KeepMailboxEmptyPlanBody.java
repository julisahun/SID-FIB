package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;

import bdi4jade.plan.DefaultPlan;
import org.json.JSONObject;

import bdi4jade.goal.*;
import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents.BDIAgent;
import jade.lang.acl.ACLMessage;

public class KeepMailboxEmptyPlanBody extends AbstractPlanBody {
	private ACLMessage msgReceived;

	@Override
	public void action() {
		ACLMessage msg = this.msgReceived;
		((BDIAgent) this.myAgent).addMessage(msg);
		if (msg.getPerformative() == ACLMessage.INFORM) {
			handleInform(msg);
		} else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			// NO OP
		} else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
			handleReject(msg);
		} else {
			Goal commandSentGoal = new PredicateGoal<String>(COMMAND_SENT, false);
			((BDIAgent) this.myAgent).addGoal(commandSentGoal);
			GoalTemplate commandSentTemplate = matchesGoal(commandSentGoal);
			Plan commandSentPlan = new DefaultPlan(
					commandSentTemplate, CommandSentPlanBody.class);
			getCapability().getPlanLibrary().addPlan(commandSentPlan);
		}
		setEndState(Plan.EndState.SUCCESSFUL);
	}

	private void handleReject(ACLMessage msg) {
		String sender = msg.getSender().getLocalName();
		if (sender.equals("slave")) {
			String content = msg.getContent();
			JSONObject body = new JSONObject(content);
			String rejectedNode = body.getString("position");
			addRejectedNode(rejectedNode);

			Belief commandSent = getBeliefBase().getBelief(COMMAND_SENT);
			commandSent.setValue(false);
		}
	}

	private void addRejectedNode(String rejectedNode) {
		Belief rejectedNodeBelief = getBeliefBase().getBelief(REJECTED_NODES);
		HashSet<String> rejectedNodes = (HashSet<String>) rejectedNodeBelief.getValue();
		rejectedNodes.add(rejectedNode);
		rejectedNodeBelief.setValue(rejectedNodes);
	}

	private void handleInform(ACLMessage msg) {
		String sender = msg.getSender().getLocalName();
		String content = msg.getContent();
		JSONObject body = new JSONObject(content);
		if (sender.equals("slave")) {
			String status = body.getString("status");
			if (status.equals("failure")) {
				String rejectedNode = body.getString("position");
				addRejectedNode(rejectedNode);
			} else if (status.equals("finished")) {
				Belief isFullExplored = getBeliefBase().getBelief(IS_FULL_EXPLORED);
				isFullExplored.setValue(true);
			} else if (status.equals("pong")) {
				HashMap map = new HashMap<>();
				String stringMap = body.getString("map");
				JSONObject jsonMap = new JSONObject(stringMap);
				for (String node : jsonMap.keySet()) {
					HashSet<String> neighbors = new HashSet<>();

					for (Object neighbor : jsonMap.getJSONObject(node).getJSONArray("neighbors")) {
						neighbors.add((String) neighbor);
					}
					Boolean closed = neighbors.size() > 0;
					map.put(node, new Couple<Boolean, HashSet<String>>(closed, neighbors));
				}
				Belief mapBelief = getBeliefBase().getBelief(MAP);
				mapBelief.setValue(map);
				Belief b = getBeliefBase().getBelief(IS_SLAVE_ALIVE);
				b.setValue(true);
				return;
			}
			Belief commandSent = getBeliefBase().getBelief(COMMAND_SENT);
			commandSent.setValue(false);
			updateMap(body.getString("map"));
		}
	}

	private HashMap parseMap(String stringMap) {
		JSONObject map = new JSONObject(stringMap);
		HashMap<String, Couple<Boolean, HashSet<String>>> parsedMap = new HashMap<>();
		for (String node : map.keySet()) {
			JSONObject nodeObject = map.getJSONObject(node);
			String status = nodeObject.getString("status");
			HashSet<String> neighbors = new HashSet<>();
			for (Object neighbor : nodeObject.getJSONArray("neighbors")) {
				neighbors.add(neighbor.toString());
			}
			parsedMap.put(node, new Couple<Boolean, HashSet<String>>(status.equals("closed"), neighbors));
		}
		return parsedMap;
	}

	private void updateMap(String newMap) {
		HashMap<String, HashSet<String>> parsedMap = parseMap(newMap);
		Belief mapBelief = getBeliefBase().getBelief(MAP);
		HashMap<String, Couple<Boolean, HashSet<String>>> map = (HashMap<String, Couple<Boolean, HashSet<String>>>) mapBelief
				.getValue();
		mapBelief.setValue(parsedMap);

	}

	@Parameter(direction = Parameter.Direction.IN)
	public void setMessage(ACLMessage msgReceived) {
		this.msgReceived = msgReceived;
	}

	private GoalTemplate matchesGoal(Goal goalToMatch) {
		return new GoalTemplate() {
			@Override
			public boolean match(Goal goal) {
				return goal == goalToMatch;
			}
		};
	}

}
