package eu.su.mas.dedaleEtu.sid.grupo03.core;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

import java.io.Serializable;

public class Map implements Serializable {

  private HashMap<String, Node> nodes;

  public Map() {
    this.nodes = new HashMap<String, Node>();
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
  }

  public void clear() {
    this.nodes = new HashMap<String, Node>();
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
