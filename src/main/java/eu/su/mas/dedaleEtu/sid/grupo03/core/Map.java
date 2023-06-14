package eu.su.mas.dedaleEtu.sid.grupo03.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import java.io.Serializable;

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

  public void clear() {
    this.nodes = new HashMap<String, Node>();
    this.openNodes = new HashSet<String>();
  }

  public void visit(String nodeId) {
    this.nodes.get(nodeId).visit();
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
