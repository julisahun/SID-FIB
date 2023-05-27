package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;

public class Node {

  public enum Status {
    OPEN,
    CLOSED
  }

  private Status status = Status.OPEN;
  private String id;
  private Integer timeVisited = 0;
  private HashSet<String> neighbors = new HashSet<String>();
  private List<Couple<String, Integer>> observations = new ArrayList<Couple<String, Integer>>();

  public Node(JSONObject json) {
    this.id = json.getString("id");
    this.status = json.getString("status").equals("closed") ? Status.CLOSED : Status.OPEN;
    this.neighbors = new HashSet<>();
    JSONArray neighbors = json.getJSONArray("neighbors");
    for (int i = 0; i < neighbors.length(); i++) {
      this.neighbors.add(neighbors.getString(i));
    }
    JSONArray observations = json.getJSONArray("observations");
    for (int i = 0; i < observations.length(); i++) {
      JSONObject observation = observations.getJSONObject(i);
      this.observations.add(new Couple<String, Integer>(observation.getString("observation"),
          observation.getInt("value")));
    }
  }

  public Node(String id, Status status, HashSet<String> neighbors, List<Couple<Observation, Integer>> observations) {
    this.status = status;
    this.neighbors = neighbors;
    this.id = id;
    this.observations = new ArrayList<Couple<String, Integer>>();
    for (Couple<Observation, Integer> observation : observations) {
      this.observations.add(new Couple<String, Integer>(observation.getLeft().getName(), observation.getRight()));
    }
  }

  public Node(String id, Status status, HashSet<String> neighbors) {
    this.status = status;
    this.neighbors = neighbors;
    this.id = id;
  }

  public Status getStatus() {
    return this.status;
  }

  public String getId() {
    return this.id;
  }

  public Integer getTimesVisited() {
    return this.timeVisited;
  }

  public void visit() {
    this.timeVisited++;
  }

  public HashSet<String> getNeighbors() {
    return this.neighbors;
  }

  public List<Couple<String, Integer>> getObservations() {
    return this.observations;
  }

  public void setObservations(List<Couple<String, Integer>> observations) {
    this.observations = observations;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setNeighbors(HashSet<String> neighbors) {
    this.neighbors = neighbors;
  }

  public void addNeighbor(String neighbor) {
    this.neighbors.add(neighbor);
  }

  public void mergeObs(List<Couple<Observation, Integer>> observations) {
    for (Couple<Observation, Integer> observation : observations) {
      Observation obs = observation.getLeft();
      boolean found = false;
      for (Couple<String, Integer> thisObservation : this.observations) {
        String thisObs = thisObservation.getLeft();
        if (thisObs.equals(obs.getName())) {
          found = true;
          break;
        }
      }
      if (!found) {
        this.observations.add(new Couple<String, Integer>(observation.getLeft().getName(), observation.getRight()));
      }
    }
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("id", this.id);
    json.put("status", this.status.equals(Status.OPEN) ? "open" : "closed");
    JSONArray neighbors = new JSONArray();
    for (String neighbor : this.neighbors) {
      neighbors.put(neighbor);
    }
    json.put("neighbors", neighbors);
    JSONArray observations = new JSONArray();
    for (Couple<String, Integer> observation : this.observations) {
      JSONObject observationJson = new JSONObject();
      observationJson.put("observation", observation.getLeft());
      observationJson.put("value", observation.getRight());
      observations.put(observationJson);
    }
    json.put("observations", observations);
    return json;
  }

  public JSONObject difference(Node node) {
    JSONObject json = new JSONObject();
    if (!this.id.equals(node.getId()))
      return null;

    if (!this.status.equals(node.getStatus())) {
      json.put("status", node.getStatus().equals(Status.OPEN) ? "open" : "closed");
    }
    if (!this.timeVisited.equals(node.getTimesVisited())) {
      json.put("timesVisited", node.getTimesVisited());
    }
    JSONArray neighbors = new JSONArray();
    for (String neighbor : node.getNeighbors()) {
      if (!this.neighbors.contains(neighbor)) {
        neighbors.put(neighbor);
      }
    }
    if (neighbors.length() > 0) {
      json.put("neighbors", neighbors);
    }
    Boolean sameObservations = true;
    for (Couple<String, Integer> observation : node.getObservations()) {
      Boolean isOk = false;
      String name = observation.getLeft();
      Integer value = observation.getRight();
      for (Couple<String, Integer> nodeObservation : this.observations) {
        if (nodeObservation.getLeft().equals(name) && nodeObservation.getRight().equals(value)) {
          isOk = true;
          break;
        }
      }
      if (!isOk) {
        sameObservations = false;
        break;
      }
    }
    if (!sameObservations) {
      JSONArray observations = new JSONArray();
      for (Couple<String, Integer> observation : node.getObservations()) {
        JSONObject observationJson = new JSONObject();
        observationJson.put("observation", observation.getLeft());
        observationJson.put("value", observation.getRight());
        observations.put(observationJson);
      }
    }
    return json;
  }
}
