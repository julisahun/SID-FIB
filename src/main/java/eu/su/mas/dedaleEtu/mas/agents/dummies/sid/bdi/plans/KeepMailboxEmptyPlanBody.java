package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import bdi4jade.plan.DefaultPlan;
import org.json.JSONObject;

import bdi4jade.goal.*;
import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents.BDIAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.Map;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import eu.su.mas.dedaleEtu.mas.knowledge.Node;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;

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
		if (msg.getProtocol().equals("SHARE-ONTO")) {
			updateOntology(msg.getContent());
		}
		String content = msg.getContent();
		JSONObject body = new JSONObject(content);
		String status = body.getString("status");
		if (status.equals("failure")) {
			String rejectedNode = body.getString("position");
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
			parsedMap.put(node, new Node(map.getJSONObject(node)));
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
		MapaModel newOntology = MapaModel.importOntology(stringifiedOntology);
		Belief ontologyBelief = getBeliefBase().getBelief(ONTOLOGY);
		MapaModel ontology = (MapaModel) ontologyBelief.getValue();
		ontology.learnFromOtherOntology(newOntology);
		ontologyBelief.setValue(ontology);

		Belief mapOutOfSync = getBeliefBase().getBelief(MAP_OUT_OF_SYNC);
		mapOutOfSync.setValue(true);
	}

	@Parameter(direction = Parameter.Direction.IN)
	public void setMessage(ACLMessage msgReceived) {
		this.msgReceived = msgReceived;
	}
}
