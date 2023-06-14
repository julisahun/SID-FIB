package eu.su.mas.dedaleEtu.sid.grupo03;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.sid.grupo03.behaviours.BackpackEmptier;
import eu.su.mas.dedaleEtu.sid.grupo03.behaviours.MessageMapper;
import eu.su.mas.dedaleEtu.sid.grupo03.behaviours.OntologySharer;
import eu.su.mas.dedaleEtu.sid.grupo03.behaviours.RegisterToDF;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Map;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Node;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.behaviours.Behaviour;
import dataStructures.tuple.Couple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.HashSet;

public class SituatedAgent03 extends AbstractDedaleAgent {

    private MapRepresentation map;
    private Map nodes;
    private HashSet<String> rejectedNodes = new HashSet<>();
    private HashMap<String, List<String>> messages;

    private HashMap<String, Integer> behavioursStatus = new HashMap<>();
    private HashMap<String, Behaviour> behaviours = new HashMap<>();
    private Object[] arguments;
    private String ontology;
    private String ontologySharerId;
    public String master;

    @Override
    protected void setup() {
        super.setup();
        List<Behaviour> lb = new ArrayList<>();
        this.arguments = getArguments();
        lb.add(new RegisterToDF(this, this.getAID().getLocalName(), this.getType()));
        lb.add(new MessageMapper(this));
        if (this.getType().equals("agentCollect")) {
            lb.add(new BackpackEmptier(this));
        }
        addBehaviour(new startMyBehaviours(this, lb));
        this.messages = new HashMap<>();
        this.nodes = new Map();
    }

    public void setMap(MapRepresentation map) {
        this.map = map;
    }

    public void mergeMap(MapRepresentation newMap) {
        if (this.map == null)
            this.map = newMap;
        else
            this.map.mergeMap(newMap.getSerializableGraph());
    }

    public MapRepresentation getMapRepresentation() {
        return this.map;
    }

    public void addNode(String currentNode, String newNode, List<Couple<Observation, Integer>> observations) {

        for (Couple<Observation, Integer> obs : observations) {
            if (obs.getLeft().getName().equals("WIND")) {
                return;
            }
        }
        if (!this.nodes.has(newNode)) {
            this.nodes.put(newNode, new Node(newNode, Node.Status.OPEN, new HashSet<String>(), observations));
        } else {
            this.nodes.get(newNode).mergeObs(observations);
        }
        if (currentNode.equals(newNode))
            return;
        Node value = this.nodes.get(currentNode);
        HashSet<String> neighbors = value.getNeighbors();
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

    public void updateObs(String node, Couple<String, Integer> observation) {
        System.out.println("Updating obs " + observation);
        if (!this.nodes.has(node))
            return;
        if (observation.getLeft() == null || observation.getLeft().equals("Gold")
                || observation.getLeft().equals("Diamond")) {
            System.out.println("Updating resource " + observation);
            this.nodes.get(node).updateResource(node, observation.getRight());
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

    public void registerBehaviour(String id, Behaviour b) {
        this.behavioursStatus.put(id, null);
        this.behaviours.put(id, b);
    }

    public void updateStatus(String id) {
        this.behavioursStatus.put(id, null);
    }

    public void updateStatus(String id, Integer code) {
        this.behavioursStatus.put(id, code);
    }

    public Integer getStatus(String id) {
        return this.behavioursStatus.get(id);
    }

    public Behaviour getBehaviour(String id) {
        return this.behaviours.get(id);
    }

    public void recordVisit(String nodeId) {
        this.nodes.visit(nodeId);
    }

    public String getBuffer() {
        return this.nodes.stringifyNodes();
    }

    public String clearBuffer() {
        String buffer = this.nodes.stringifyNodes();
        this.nodes.clear();
        return buffer;
    }

    public void setOntology(String ontology) {
        OntologySharer oldBehaviour = (OntologySharer) this.getBehaviour(this.ontologySharerId);
        if (oldBehaviour == null) {
            Behaviour b = new OntologySharer(this, this.ontology);
            this.ontologySharerId = Utils.uuid();
            this.registerBehaviour(this.ontologySharerId, b);
            this.addBehaviour(b);
        } else {
            oldBehaviour.updateOntology(ontology);
        }
        this.ontology = ontology;
    }

    public String getType() {
        String typeField = this.arguments[0].toString().split(";")[1];
        String type = typeField.split(":")[1].trim();
        if (type.equals("AgentExplo"))
            return "agentExplo";
        if (type.equals("AgentTanker"))
            return "agentTanker";
        if (type.equals("AgentCollect"))
            return "agentCollect";

        return "agentExplo";
    }
}
