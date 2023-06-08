package eu.su.mas.dedaleEtu.sid.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

  public void visit(String nodeId) {
    this.nodes.get(nodeId).visit();
  }
}
