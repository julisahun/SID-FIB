package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class Node {

  public enum Status {
    OPEN,
    CLOSED
  }

  private Status status;
  private HashSet<String> neighbors;

  public Node(JSONObject json) {
    this.status = json.getString("status").equals("closed") ? Status.CLOSED : Status.OPEN;
    this.neighbors = new HashSet<>();
    JSONArray neighbors = json.getJSONArray("neighbors");
    for (int i = 0; i < neighbors.length(); i++) {
      this.neighbors.add(neighbors.getString(i));
    }
  }

  public Node(Status status, HashSet<String> neighbors) {
    this.status = status;
    this.neighbors = neighbors;
  }

  public Status getStatus() {
    return this.status;
  }

  public HashSet<String> getNeighbors() {
    return this.neighbors;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setNeighbors(HashSet<String> neighbors) {
    this.neighbors = neighbors;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("status", this.status.equals(Status.OPEN) ? "open" : "closed");
    JSONArray neighbors = new JSONArray();
    for (String neighbor : this.neighbors) {
      neighbors.put(neighbor);
    }
    json.put("neighbors", neighbors);
    return json;
  }

}
