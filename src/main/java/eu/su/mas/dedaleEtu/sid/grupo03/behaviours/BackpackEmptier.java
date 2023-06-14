package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import java.util.HashSet;
import java.util.List;

import org.json.JSONObject;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class BackpackEmptier extends TickerBehaviour {
  public BackpackEmptier(Agent a) {
    super(a, 500);
  }

  public void onTick() {
    System.out.println("Emptying backpack");
    HashSet<String> tankerAgents = Utils.getTankers(this.myAgent);
    SituatedAgent03 situated = (SituatedAgent03) this.myAgent;
    for (String tanker : tankerAgents) {
      List<Couple<Observation, Integer>> spaceBeforeDrop = situated.getBackPackFreeSpace();
      Boolean b = situated.emptyMyBackPack(tanker);
      if (b) {
        List<Couple<Observation, Integer>> spaceAfterDrop = situated.getBackPackFreeSpace();

        for (Couple<Observation, Integer> resourceBeforeDrop : spaceBeforeDrop) {
          for (Couple<Observation, Integer> resourceAfterDrop : spaceAfterDrop) {
            if (resourceBeforeDrop.getLeft().getName().equals(resourceAfterDrop.getLeft().getName())) {
              if (resourceBeforeDrop.getRight() > resourceAfterDrop.getRight()) {
                System.out.println("Dropped " + (resourceBeforeDrop.getRight() - resourceAfterDrop.getRight()) + " "
                    + resourceBeforeDrop.getLeft().getName());
                JSONObject msg = new JSONObject();
                msg.put("resourcesCapacity", Utils.getBackPackFreeSpace(situated));
                situated.addBehaviour(new MessageSender(situated, ACLMessage.INFORM, msg));
              }
            }
          }
        }
        return;
      }
    }
  }
}
