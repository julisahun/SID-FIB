package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.NodeInfo;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.NodeType;
import javafx.util.Pair;

public class Map implements Serializable {

  private HashMap<String, Node> nodes;
  private HashSet<String> openNodes = new HashSet<String>();

  public Map() {
    this.nodes = new HashMap<String, Node>();
  }

  public Map(HashMap<String, Node> nodes) {
    this.nodes = nodes;
  }

  public Set<String> keySet() {
    return this.nodes.keySet();
  }

  public Node get(String key) {
    return this.nodes.get(key);
  }

  public Boolean has(String key) {
    return this.nodes.containsKey(key);
  }

  public void put(String key, Node value) {
    this.nodes.put(key, value);
    if (value.getStatus().equals(Node.Status.OPEN)) {
      this.openNodes.add(key);
    }
  }

  public String getOpenNode() {
    if (this.openNodes.isEmpty())
      return null;
    return this.openNodes.iterator().next();
  }

  public void update(Map patchUpdate, MapaModel ontology) {
    for (String node : patchUpdate.keySet()) {
      if (this.nodes.get(node) != null) {
        JSONObject complementaryInfo = this.nodes.get(node).difference(patchUpdate.get(node));
        if (complementaryInfo == null || complementaryInfo.isEmpty())
          continue;

        updateNode(node, complementaryInfo, ontology);

      } else {
        Node newNode = patchUpdate.get(node);
        ontology.addNode(node,
            newNode.getStatus() == Node.Status.OPEN ? NodeType.Open : NodeType.Closed);
        for (String neighbor : newNode.getNeighbors()) {
          ontology.addAdjancency(node, neighbor);
        }
        this.put(node, newNode);
      }
    }
  }

  private void syncOpenNodes(Set<String> openNodes) {
    for (String node : openNodes) {
      if (!this.nodes.containsKey(node)) {
        Node newNode = new Node(node, Node.Status.OPEN);
        this.put(node, newNode);
      } else {
        this.nodes.get(node).setStatus(Node.Status.OPEN);
      }
      // update cache
      this.openNodes.add(node);
    }
  }

  private void syncClosedNodes(Set<String> closedNodes) {
    for (String node : closedNodes) {
      if (!this.nodes.containsKey(node)) {
        Node newNode = new Node(node, Node.Status.CLOSED);
        this.put(node, newNode);
      } else {
        this.nodes.get(node).setStatus(Node.Status.CLOSED);
      }
      // update cache
      this.openNodes.remove(node);
    }
  }

  private void syncWindyNodes(Set<String> windyNodes) {
    for (String node : windyNodes) {
      if (!this.nodes.containsKey(node)) {
        this.nodes.put(node, new Node(node, Node.Status.OPEN, List.of(new Couple<>("WIND", 0))));
        this.openNodes.add(node);
      } else {
        this.nodes.get(node).addObservation(new Couple<>("WIND", 0));
      }
    }
  }

  private void syncEdges(Set<Pair<String, String>> edges) {
    for (Pair<String, String> edge : edges) {
      String node1 = edge.getKey();
      String node2 = edge.getValue();
      if (!this.nodes.containsKey(node1)) {
        Node newNode = new Node(node1, Node.Status.OPEN);
        newNode.addNeighbor(node2);
        this.put(node1, newNode);
      } else {
        this.nodes.get(node1).addNeighbor(node2);
      }
      if (!this.nodes.containsKey(node2)) {
        Node newNode = new Node(node2, Node.Status.OPEN);
        newNode.addNeighbor(node1);
        this.put(node2, newNode);
      } else {
        this.nodes.get(node2).addNeighbor(node1);
      }
    }
  }

  private void syncObservations(HashSet<String> nodes, MapaModel ontology) {
    for (String node : nodes) {
      try {
        NodeInfo info = ontology.getCellInfo(node);
        List<Couple<String, Integer>> observations = new ArrayList<Couple<String, Integer>>();
        if (info.diamondAmount > 0) {
          observations.add(new Couple<>("Diamond", ((Long) info.diamondAmount).intValue()));
        }
        if (info.goldAmount > 0) {
          observations.add(new Couple<>("Gold", ((Long) info.goldAmount).intValue()));
        }
        if (info.lockpickLevel > 0) {
          observations.add(new Couple<>("LockPicking", ((Long) info.lockpickLevel).intValue()));
        }
        if (info.lockpickLevel > 0) {
          observations.add(new Couple<>("Strength", ((Long) info.lockpickLevel).intValue()));
        }
        this.nodes.get(node).setObservations(observations);
      } catch (Exception e) {
        // do nothing
      }
    }
  }

  public void syncWithOntology(MapaModel ontology) {
    Set<String> openNodes = ontology.getOpenNodes();
    syncOpenNodes(openNodes);

    Set<String> closedNodes = ontology.getClosedNodes();
    syncClosedNodes(closedNodes);

    Set<String> windyNodes = ontology.getWindyNodes();
    syncWindyNodes(windyNodes);

    Set<Pair<String, String>> edges = ontology.getEdges();
    syncEdges(edges);

    HashSet<String> fullNodes = new HashSet<String>();
    fullNodes.addAll(openNodes);
    fullNodes.addAll(closedNodes);
    fullNodes.addAll(windyNodes);

    syncObservations(fullNodes, ontology);
  }

  private void updateNode(String node, JSONObject update, MapaModel ontology) {
    if (update.has("status")) {
      String status = update.getString("status");
      ontology.addNode(node, status.equals("open") ? NodeType.Open : NodeType.Closed);
      this.nodes.get(node)
          .setStatus(status.equals("open") ? Node.Status.OPEN : Node.Status.CLOSED);
      if (status.equals("open")) {
        this.openNodes.add(node);
      } else {
        this.openNodes.remove(node);
      }
    }
    if (update.has("neighbors")) {
      JSONArray neighbors = update.getJSONArray("neighbors");
      for (int i = 0; i < neighbors.length(); i++) {
        String neighbor = neighbors.getString(i);
        ontology.addAdjancency(node, neighbor);
        this.nodes.get(node).addNeighbor(neighbor);
      }
    }
    if (update.has("observations")) {
      JSONArray observations = update.getJSONArray("observations");
      List<Couple<String, Integer>> observationsList = new ArrayList<Couple<String, Integer>>();
      long diamondAmount = 0;
      long goldAmount = 0;
      long lockpickLevel = 0;
      for (int i = 0; i < observations.length(); i++) {
        JSONObject observation = observations.getJSONObject(i);
        String name = observation.getString("observation");
        Integer value = observation.getInt("value");
        observationsList.add(new Couple<String, Integer>(name, value));
        if (name.equals("Gold")) {
          goldAmount += value;
        } else if (name.equals("Diamond")) {
          diamondAmount += value;
        } else if (name.equals("LockPicking")) {
          lockpickLevel += value;
        }
      }
      ontology.addNodeInfo(node, diamondAmount, goldAmount, lockpickLevel);
      this.nodes.get(node).setObservations(observationsList);
    }
    if (update.has("timesVisited")) {
      Integer timesVisited = update.getInt("timesVisited");
      this.nodes.get(node).setTimesVisited(timesVisited);
    }
  }
}
