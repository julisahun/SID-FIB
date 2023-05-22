package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.ontologyBehaviours.IndividualAdder;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.ontologyBehaviours.PropertyAdder;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.ontologyBehaviours.PropertyRemover;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Property;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.List;
import java.util.ArrayList;

import org.graphstream.stream.sync.SourceTime;

/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 *
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.
 * This (non optimal) behaviour is done until all nodes are explored.
 *
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 *
 * @author hc
 */
public class ExplorerAndUpdaterBehaviour extends SimpleBehaviour {
    private static final long serialVersionUID = 8567689731496787661L;
    private boolean finished = false;
    private List<Behaviour> callbacks;

    /**
     * Current knowledge of the agent regarding the environment
     */
    private MapRepresentation myMap;

    /**
     * @param myAgent    the agent using this behaviour
     * @param myMap      known map of the world the agent is living in
     * @param agentNames name of the agents to share the map with
     */
    public ExplorerAndUpdaterBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap,
            List<Behaviour> callbacks) {
        super(myAgent);
        this.myMap = myMap;
        this.callbacks = callbacks;
    }

    @Override
    public void action() {
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }

        // 0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();

        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
                    .observe();// myPosition

            // Just added here to let you see what the agent is doing, otherwise it will be
            // too quick
            // 1) remove the current node from openlist and add it to closedNodes.
            this.myMap.addNode(myPosition, MapAttribute.closed);

            // 2) get the surrounding nodes and, if not in closedNodes, add them to open
            // nodes.
            String nextNode = null;
            for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft().getLocationId();
                boolean safeNode = isSafeNode(lob);
                if (!safeNode) {
                    this.myMap.addNode(nodeId, MapAttribute.closed);
                }
                boolean isNewNode = this.myMap.addNewNode(nodeId);
                // the node may exist, but not necessarily the edge
                if (!myPosition.equals(nodeId)) {
                    this.myMap.addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode && safeNode)
                        nextNode = nodeId;
                }
            }

            // 3) while openNodes is not empty, continues.
            if (!this.myMap.hasOpenNode()) {
                // Explo finished
                finished = true;
                System.out
                        .println(this.myAgent.getLocalName() + " - Exploration successufully done, behaviour removed.");
            } else {
                // 4) select next move.
                // 4.1 If there exist one open node directly reachable, go for it,
                // otherwise choose one from the openNode list, compute the shortestPath and go
                // for it
                if (nextNode == null) {
                    // no directly accessible openNode
                    // chose one, compute the path and take the first step.
                    nextNode = this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);
                }

                // 5) At each time step, the agent check if he received a graph from a teammate.
                // If it was written properly, this sharing action should be in a dedicated
                // behaviour set.
                MessageTemplate msgTemplate = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("SHARE-TOPO"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
                if (msgReceived != null) {
                    try {
                        SerializableSimpleGraph<String, MapAttribute> sgreceived = (SerializableSimpleGraph<String, MapAttribute>) msgReceived
                                .getContentObject();
                        this.myMap.mergeMap(sgreceived);
                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                ((AbstractDedaleAgent) this.myAgent).addBehaviour(
                        new PropertyAdder(this.myAgent, this.myAgent.getLocalName(), "isAt", nextNode, false));
                ((AbstractDedaleAgent) this.myAgent).addBehaviour(
                        new PropertyRemover(this.myAgent, this.myAgent.getLocalName(), "isAt", myPosition, false));
                ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNode));
            }
        }
    }

    private boolean isSafeNode(Couple<Location, List<Couple<Observation, Integer>>> lob) {
        boolean safeNode = true;
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
        String position = lob.getLeft().getLocationId();
        this.myAgent.addBehaviour(new IndividualAdder(this.myAgent, "Node", position));
        this.myAgent.addBehaviour(new PropertyAdder(myAgent, position, "location", position));
        if (!myPosition.equals(position)) {
            this.myAgent.addBehaviour(new PropertyAdder(myAgent, myPosition, "isNextTo", position, false));
            this.myAgent.addBehaviour(new PropertyAdder(myAgent, position, "isNextTo", myPosition, false));
        }

        List<Property> properties = new ArrayList<>();
        String resource = "";
        for (Couple<Observation, Integer> ob : lob.getRight()) {
            if (ob.getLeft().getName().equals("WIND")) {
                this.myAgent.addBehaviour(new PropertyAdder(myAgent, position, "hasTraceFrom", "Well"));
                safeNode = false;
            } else if (isResource(ob.getLeft().getName())) {
                resource = ob.getLeft().getName();
                String resourceId = resource + position;
                this.myAgent.addBehaviour(new IndividualAdder(this.myAgent, resource, resourceId));
                this.myAgent.addBehaviour(new PropertyAdder(this.myAgent, position, "hasResource", resource));
                properties.add(new Property(resourceId, "isAt", position, false));
            } else {
                String name = ob.getLeft().getName();
                Integer value = ob.getRight();
                properties.add(new Property(resource + position, name, value.toString()));
            }
        }
        for (Property p : properties) {
            String from;
            if (p.getFrom() == position)
                from = resource + position;
            else
                from = p.getFrom();
            this.myAgent.addBehaviour(new PropertyAdder(myAgent, from, p.getName(), p.getTo(), p.getGeneric()));
        }
        return safeNode;
    }

    private boolean isResource(String name) {
        return name.equals("Gold") || name.equals("Diamond");
    }

    @Override
    public boolean done() {
        if (finished) {
            for (Behaviour callback : callbacks) {
                this.myAgent.addBehaviour(callback);
            }
        }
        return finished;
    }
}
