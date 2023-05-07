package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import org.json.JSONObject;
import java.util.function.Consumer;
import java.util.HashMap;

public class OntologyUpdaterOnMessage extends OneShotBehaviour {
  HashMap<String, Consumer<String>> actions = new HashMap<String, Consumer<String>>();

  public OntologyUpdaterOnMessage(Agent a) {
    super(a);
  }

  private Consumer<String> getEntityAction() {
    return (payload) -> {
      JSONObject object = new JSONObject(payload);
      String type = object.get("type").toString();
      String name = object.get("name").toString();
      String position = object.get("position").toString();
      Behaviour EntityAdder = new IndividualAdder(this.myAgent, type, name);
      Behaviour PositionAdder = new PropertyAdder(this.myAgent, name, "isAt", position, false);
      AbstractDedaleAgent agent = (AbstractDedaleAgent) this.myAgent;
      agent.addBehaviour(EntityAdder);
      agent.addBehaviour(PositionAdder);
    };
  }

  private Consumer<String> getResourceAction() {
    return (payload) -> {
      JSONObject object = new JSONObject(payload);
      String type = object.get("type").toString();
      String name = object.get("name").toString();
      String position = object.get("position").toString();
      String strength = object.get("strength").toString();
      String lockIsOpen = object.get("lockIsOpen").toString();
      String lockPicking = object.get("lockPicking").toString();

      Behaviour ResourceAdder = new IndividualAdder(this.myAgent, type, name);
      Behaviour PositionAdder = new PropertyAdder(this.myAgent, name, "isAt", position, false);
      Behaviour StrenghtAdder = new PropertyAdder(this.myAgent, name, "Strength", strength);
      Behaviour LockIsOpenAdder = new PropertyAdder(this.myAgent, name, "LockIsOpen", lockIsOpen);
      Behaviour LockPickingAdder = new PropertyAdder(this.myAgent, name, "LockPicking", lockPicking);

      AbstractDedaleAgent agent = (AbstractDedaleAgent) this.myAgent;
      agent.addBehaviour(ResourceAdder);
      agent.addBehaviour(PositionAdder);
      agent.addBehaviour(StrenghtAdder);
      agent.addBehaviour(LockIsOpenAdder);
      agent.addBehaviour(LockPickingAdder);
    };
  }

  private void buildActionsMap() {
    this.actions.put("Entity", this.getEntityAction());
    this.actions.put("Resource", this.getResourceAction());
  }

  @Override
  public void action() {
    this.buildActionsMap();
    AbstractDedaleAgent agent = (AbstractDedaleAgent) this.myAgent;
    agent.addBehaviour(new Listener(this.myAgent, actions));
  }
}
