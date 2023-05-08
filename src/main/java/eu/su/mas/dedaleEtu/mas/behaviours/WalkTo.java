package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Location;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class WalkTo extends SimpleBehaviour {
  private String target;
  private AbstractDedaleAgent agent;
  private MapRepresentation map;
  private Queue<String> route;
  private String currentPosition;

  public WalkTo(Agent a, String position, MapRepresentation map) {
    super(a);
    this.target = position;
    this.agent = (AbstractDedaleAgent) a;
    this.map = map;
  }

  public WalkTo(Agent a, String position) {
    super(a);
    this.target = position;
    this.agent = (AbstractDedaleAgent) a;
  }

  @Override
  public void action() {
    if (this.map == null) {
      this.map = new MapRepresentation();
    }
    this.currentPosition = this.agent.getCurrentPosition().getLocationId();
    if (this.currentPosition.equals(this.target))
      return;

    this.map.addNode(currentPosition, MapAttribute.closed);
    if (this.route == null) {
      try {
        List<String> route = this.map.getShortestPath(currentPosition, target);
        this.route = new LinkedList<>(route);
      } catch (Exception e) {
        this.exploreAround();
        if (!this.map.hasOpenNode()) {
          sendErrorMessage();
          return;
        }
        this.moveToOpenNode();
        return;
      }
    }
    String nextNode = this.route.poll();
    this.move(nextNode);
  }

  private void sendErrorMessage() {
    this.onEnd();
  }

  private void exploreAround() {
    List<Couple<Location, List<Couple<Observation, Integer>>>> neighbors = ((AbstractDedaleAgent) this.myAgent)
        .observe();
    for (Couple<Location, List<Couple<Observation, Integer>>> neighbor : neighbors) {
      String nodeId = neighbor.getLeft().getLocationId();
      boolean safeNeighbor = true;
      for (Couple<Observation, Integer> observation : neighbor.getRight()) {
        if (observation.getLeft().getName().equals("WIND")) {
          safeNeighbor = false;
          break;
        }
      }
      if (!safeNeighbor)
        continue;
      this.map.addNewNode(nodeId);
      if (!currentPosition.equals(nodeId))
        this.map.addEdge(currentPosition, nodeId);
    }
  }

  private void moveToOpenNode() {
    String nextMove = this.map.getShortestPathToClosestOpenNode(currentPosition).get(0);
    this.move(nextMove);
  }

  private void move(String nextNode) {
    this.agent.moveTo(new gsLocation(nextNode));
  }

  @Override
  public boolean done() {
    boolean done = this.currentPosition.equals(this.target);
    if (done) {
      ((SituatedAgent) this.agent).setMap(this.map);
    }
    return done;
  }
}
