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
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.NodeType;

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
  }
}
