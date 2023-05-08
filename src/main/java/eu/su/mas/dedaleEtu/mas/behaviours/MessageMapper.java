package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.function.Consumer;

import jade.core.behaviours.Behaviour;
import org.json.JSONObject;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class MessageMapper extends OneShotBehaviour {
  private final AbstractDedaleAgent agent;

  public MessageMapper(Agent a) {
    super(a);
    this.agent = (AbstractDedaleAgent) a;
  }

  private void updatePosition(String body) {
    try {
      JSONObject json = new JSONObject(body);
      String position = json.getString("position");
      this.agent.addBehaviour(new WalkTo(this.agent, position, ((SituatedAgent) this.agent).getMap()));
    } catch (Exception e) {
      System.out.println("Error parsing JSON message");
    }
  }

  private void updateMap(String body) {
    System.out.println("Updating map");
  }

  @Override
  public void action() {
    HashMap<String, Consumer<String>> actions = new HashMap<>();
    actions.put("position", this::updatePosition);
    actions.put("map", this::updateMap);
    this.myAgent.addBehaviour(new Listener(this.myAgent, actions));
  }

}
