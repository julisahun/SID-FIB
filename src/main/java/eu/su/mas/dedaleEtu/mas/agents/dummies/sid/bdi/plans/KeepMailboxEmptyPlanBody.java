package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import bdi4jade.plan.DefaultPlan;

import org.json.JSONArray;
import org.json.JSONObject;

import bdi4jade.goal.*;
import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents.BDIAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Map;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import eu.su.mas.dedaleEtu.mas.knowledge.Node;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;

public class KeepMailboxEmptyPlanBody extends AbstractPlanBody {
	private ACLMessage msgReceived;

	@Override
	public void action() {
		try {
			ACLMessage msg = this.msgReceived;
			((BDIAgent) this.myAgent).addMessage(msg);
			if (msg.getPerformative() == ACLMessage.INFORM) {
				handleInform(msg);
			} else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				// NO OP
			} else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
				handleReject(msg);
			} else {
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					BDIAgent agent = (BDIAgent) this.myAgent;
					agent.situatedName = msg.getContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		setEndState(Plan.EndState.SUCCESSFUL);
	}

	private void handleReject(ACLMessage msg) {
		String sender = msg.getSender().getLocalName();
		if (sender.equals("slave")) {
			String content = msg.getContent();
			JSONObject body = new JSONObject(content);
			String rejectedNode = body.getString("command");
			addRejectedNode(rejectedNode);

			Belief commandSent = getBeliefBase().getBelief(COMMAND_SENT);
			commandSent.setValue(false);
		}
	}

	private void addRejectedNode(String rejectedNode) {
		Belief rejectedNodeBelief = getBeliefBase().getBelief(REJECTED_NODES);
		HashMap<String, Integer> rejectedNodes = (HashMap<String, Integer>) rejectedNodeBelief.getValue();
		rejectedNodes.put(rejectedNode, 0);
		rejectedNodeBelief.setValue(rejectedNodes);
	}

	private void handleInform(ACLMessage msg) {
		if (msg.getProtocol().equals("SHARE-ONTO")) {
			updateOntology(msg.getContent());
			return;
		}
		String content = msg.getContent();
		JSONObject body = new JSONObject(content);
		String status = body.getString("status");
		if (status.equals("error")) {
			String rejectedNode = body.getString("command");
			addRejectedNode(rejectedNode);
		} else if (status.equals("finished")) {
		} else if (status.equals("pong")) {
			String agentType = body.getString("agentType");
			Belief b = getBeliefBase().getBelief(agentType + "Alive");
			b.setValue(true);
		}
		Belief currentPosition = getBeliefBase().getBelief(CURRENT_SITUATED_POSITION);
		currentPosition.setValue(body.getString("position"));
		Belief commandSent = getBeliefBase().getBelief(SITUATED_COMMANDED);
		commandSent.setValue(false);
		pushMapUpdate(body.getString("map"));
	}

	private Map parseMap(String stringMap) {
		JSONObject map = new JSONObject(stringMap);
		Map parsedMap = new Map();
		for (String node : map.keySet()) {
			Node n = new Node(map.getJSONObject(node));
			parsedMap.put(node, n);
		}
		return parsedMap;
	}

	private void pushMapUpdate(String newMap) {
		Map parsedMap = parseMap(newMap);

		Belief currentPendingUpdatesBelief = getBeliefBase().getBelief(MAP_UPDATES);
		Queue currentPendingUpdates = (Queue) currentPendingUpdatesBelief.getValue();
		currentPendingUpdates.add(parsedMap);
		currentPendingUpdatesBelief.setValue(currentPendingUpdates);
	}

	private void updateOntology(String stringifiedOntology) {
		System.out.println("Master in charge of " + ((BDIAgent) this.myAgent).situatedName + " received ontology");
		Belief currentOntologyHash = getBeliefBase().getBelief(ONTOLOGY_HASH);
		if (((Integer) currentOntologyHash.getValue()) == stringifiedOntology.hashCode())
			// avoid getting spammed by some agent
			return;

		MapaModel newOntology = MapaModel.importOntology(stringifiedOntology);
		Belief ontologyBelief = getBeliefBase().getBelief(ONTOLOGY);
		MapaModel ontology = (MapaModel) ontologyBelief.getValue();
		ontology.learnFromOtherOntology(newOntology);
		ontologyBelief.setValue(ontology);
		currentOntologyHash.setValue(ontology.getOntology().hashCode());

		JSONObject newMap = new JSONObject();
		JSONArray openNodes = new JSONArray();
		for (String node : ontology.getOpenNodes()) {
			openNodes.put(node);
		}
		JSONArray closedNodes = new JSONArray();
		for (String node : ontology.getClosedNodes()) {
			openNodes.put(node);
		}
		JSONArray edges = new JSONArray();
		for (Pair<String, String> edge : ontology.getEdges()) {
			JSONObject edgeJson = new JSONObject();
			edgeJson.put("from", edge.getKey());
			edgeJson.put("to", edge.getValue());
			edges.put(edgeJson);
		}
		newMap.put("openNodes", openNodes);
		newMap.put("closedNodes", closedNodes);
		newMap.put("edges", edges);
		JSONObject body = new JSONObject();
		body.put("map", newMap);
		Utils.sendMessage(myAgent, ACLMessage.INFORM, "map:" + body.toString(),
				((BDIAgent) this.myAgent).situatedName);
	}

	@Parameter(direction = Parameter.Direction.IN)
	public void setMessage(ACLMessage msgReceived) {
		this.msgReceived = msgReceived;
	}
}
