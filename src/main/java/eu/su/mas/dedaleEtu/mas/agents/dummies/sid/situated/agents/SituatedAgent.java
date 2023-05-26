package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.agents;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.MessageMapper;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.RegisterToDF;
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
    private HashMap<String, Node> nodes;
    private HashSet<String> rejectedNodes = new HashSet<>();
    private HashMap<String, List<String>> messages;

    private HashMap<String, Couple<BehaviourStatus, Integer>> behavioursStatus = new HashMap<>();
    private HashMap<String, Behaviour> behaviours = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();
        List<Behaviour> lb = new ArrayList<>();
        lb.add(new RegisterToDF(this, "explorer", "situated"));
        lb.add(new MessageMapper(this));
        addBehaviour(new startMyBehaviours(this, lb));
        this.messages = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public void setMap(MapRepresentation newMap) {
        if (this.map == null)
            this.map = newMap;
        else
            this.map.mergeMap(newMap.getSerializableGraph());
    }

    public MapRepresentation getMap() {
        return this.map;
    }

    public void printNodes() {
        System.out.println(this.stringifyNodes());
    }

    public void addNode(String node1, String node2, List<Couple<Observation, Integer>> observations) {
        Node value = this.nodes.get(node1);
        HashSet<String> neighbors = value.getNeighbors();
        if (neighbors == null) {
            neighbors = new HashSet<String>();
        }
        for (String neighbor : neighbors) {
            if (neighbor.equals(node2) && this.nodes.containsKey(node2)) {
                return;
            }
        }
        neighbors.add(node2);
        this.nodes.put(node1, new Node(node1, value.getStatus(), neighbors, value.getObservations()));
        if (!this.nodes.containsKey(node2)) {
            this.nodes.put(node2, new Node(node2, Node.Status.OPEN, new HashSet<String>(), observations));
        }
    }

    public void addNode(String node) {
        if (!this.nodes.containsKey(node)) {
            this.nodes.put(node, new Node(node, Node.Status.CLOSED, new HashSet<String>()));
        }
    }

    public void closeNode(String node) {
        if (this.nodes.containsKey(node)) {
            this.nodes.put(node, new Node(node, Node.Status.CLOSED, this.nodes.get(node).getNeighbors(),
                    this.nodes.get(node).getObservations()));
        }
    }

    public void updateObs(String node, List<Couple<Observation, Integer>> observations) {
        if (this.nodes.containsKey(node)) {
            Node nodeInfo = this.nodes.get(node);
            nodeInfo.mergeObs(observations);
            this.nodes.put(node, nodeInfo);
        }
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
}
