package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.agents;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.MessageMapper;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.RegisterToDF;
import eu.su.mas.dedaleEtu.mas.knowledge.Map;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Node;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils.BehaviourStatus;
import jade.core.behaviours.Behaviour;
import dataStructures.tuple.Couple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashSet;

public class SituatedAgent extends AbstractDedaleAgent {

    private MapRepresentation map;
    private Map nodes;
    private HashSet<String> rejectedNodes = new HashSet<>();
    private HashMap<String, List<String>> messages;

    private HashMap<String, Couple<BehaviourStatus, Integer>> behavioursStatus = new HashMap<>();
    private HashMap<String, Behaviour> behaviours = new HashMap<>();
    private Object[] arguments;

    @Override
    protected void setup() {
        super.setup();
        List<Behaviour> lb = new ArrayList<>();
        this.arguments = getArguments();
        lb.add(new RegisterToDF(this, "situated", this.getType()));
        lb.add(new MessageMapper(this));
        addBehaviour(new startMyBehaviours(this, lb));
        this.messages = new HashMap<>();
        this.nodes = new Map();
    }

    public void setMap(MapRepresentation newMap) {
        if (this.map == null)
            this.map = newMap;
        else
            this.map.mergeMap(newMap.getSerializableGraph());
    }

    public MapRepresentation getMapRepresentation() {
        return this.map;
    }

    public void printNodes() {
        System.out.println(this.stringifyNodes());
    }

    public void addNode(String currentNode, String newNode, List<Couple<Observation, Integer>> observations) {
        if (!this.nodes.has(newNode)) {
            this.nodes.put(newNode, new Node(newNode, Node.Status.OPEN, new HashSet<String>(), observations));
        }
        Node value = this.nodes.get(currentNode);
        HashSet<String> neighbors = value.getNeighbors();
        for (String neighbor : neighbors) {
            if (neighbor.equals(newNode)) {
                return;
            }
        }
        neighbors.add(newNode);
    }

    public void addNode(String node) {
        if (this.nodes.has(node))
            return;
        this.nodes.put(node, new Node(node, Node.Status.CLOSED, new HashSet<String>()));

    }

    public void closeNode(String node) {
        if (!this.nodes.has(node))
            return;
        this.nodes.get(node).setStatus(Node.Status.CLOSED);

    }

    public void updateObs(String node, List<Couple<Observation, Integer>> observations) {
        if (!this.nodes.has(node))
            return;
        Node nodeInfo = this.nodes.get(node);
        nodeInfo.mergeObs(observations);
    }

    public Boolean isAlreadyRejected(String node) {
        return this.rejectedNodes.contains(node);
    }

    public void addMessage(String header, String message) {
        if (!this.messages.containsKey(header)) {
            this.messages.put(header, new ArrayList<>());
        }
        this.messages.get(header).add(message);
    }

    public boolean getResponse(String header) {
        return this.messages.containsKey(header) && !this.messages.get(header).isEmpty();
    }

    public void registerBehaviour(String id, Behaviour b, BehaviourStatus status) {
        this.behavioursStatus.put(id, new Couple<>(status, null));
        this.behaviours.put(id, b);
    }

    public void updateStatus(String id, BehaviourStatus status) {
        this.behavioursStatus.put(id, new Couple<>(status, null));
    }

    public void updateStatus(String id, BehaviourStatus status, Integer code) {
        this.behavioursStatus.put(id, new Couple<>(status, code));
    }

    public Couple<BehaviourStatus, Integer> getStatus(String id) {
        return this.behavioursStatus.get(id);
    }

    public Behaviour getBehaviour(String id) {
        return this.behaviours.get(id);
    }

    public String stringifyNodes() {
        JSONObject json = new JSONObject();
        for (String node : this.nodes.keySet()) {
            Node nodeInfo = this.nodes.get(node);
            json.put(node, nodeInfo.toJson());
        }
        return json.toString();
    }

    public String getType() {
        String typeField = this.arguments[0].toString().split(";")[1];
        String type = typeField.split(":")[1].trim();
        if (type.equals("AgentExplo"))
            return "explorer";
        if (type.equals("AgentTanker"))
            return "tanker";
        if (type.equals("AgentCollect"))
            return "collector";
        if (type.equals("AgentGolem"))
            return "golem";

        return "explorer";
    }
}
