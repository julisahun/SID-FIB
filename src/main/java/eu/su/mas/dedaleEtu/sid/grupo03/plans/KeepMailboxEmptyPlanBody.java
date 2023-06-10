package eu.su.mas.dedaleEtu.sid.grupo03.plans;

import static eu.su.mas.dedaleEtu.sid.grupo03.core.Constants.*;

import java.util.HashMap;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.sid.grupo03.BDIAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Map;
import eu.su.mas.dedaleEtu.sid.grupo03.core.MapaModel;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Message;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Node;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;

public class KeepMailboxEmptyPlanBody extends AbstractPlanBody {
	private ACLMessage msgReceived;

	@Override
	public void action() {
		try {
			ACLMessage msg = this.msgReceived;
			Message message = Utils.messageMiddleware(this.myAgent, msg);
			((BDIAgent03) this.myAgent).addMessage(msg);
			final int performative = message.performative;
			if (performative == ACLMessage.INFORM) {
				handleInform(message);
			} else if (performative == ACLMessage.ACCEPT_PROPOSAL) {
				// NO OP
			} else if (performative == ACLMessage.REJECT_PROPOSAL) {
				handleReject(message);
			} else {
				if (performative == ACLMessage.NOT_UNDERSTOOD) {
					BDIAgent03 agent = (BDIAgent03) this.myAgent;
					agent.situatedName = "SituatedAgent0" + message.content;
				}
				if (performative == ACLMessage.CONFIRM) {
					MapaModel mapa = (MapaModel) getBeliefBase().getBelief(ONTOLOGY).getValue();
					Utils.saveOntology(mapa.model);
				}
			}
		} catch (Exception e) {
			System.out.println("Message Error " + e.getMessage());
			e.printStackTrace();
		}
		setEndState(Plan.EndState.SUCCESSFUL);
	}

	private void handleReject(Message msg) {
		String sender = msg.sender;
		if (sender.equals("slave")) {
			String content = msg.content;
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

	private void handleInform(Message msg) {
		final String content = msg.content;
		JSONObject body = new JSONObject(content);
		if (body.has("ontology")) {
			updateOntology(body.getString("ontology"));
			return;
		}
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
		body.put("ontology", ontology.getOntology());
		Utils.sendMessage(myAgent, ACLMessage.INFORM, "map:" + body.toString(),
				((BDIAgent03) this.myAgent).situatedName);
	}

	@Parameter(direction = Parameter.Direction.IN)
	public void setMessage(ACLMessage msgReceived) {
		this.msgReceived = msgReceived;
	}
}
