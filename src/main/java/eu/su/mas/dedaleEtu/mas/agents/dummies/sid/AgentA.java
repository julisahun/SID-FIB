package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedale.env.Location;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class AgentA extends AbstractDedaleAgent {

  public String myPosition = null;

  class service extends OneShotBehaviour {
    public service(AbstractDedaleAgent agent) {
      super(agent);
    }

    @Override
    public void action() {
      DFAgentDescription dfd = new DFAgentDescription();
      dfd.setName(getAID());
      ServiceDescription sd = new ServiceDescription();
      sd.setType("Explo");
      sd.setName("AgentA");
      dfd.addServices(sd);
      try {
        DFService.register(myAgent, dfd);

        this.myAgent.addBehaviour(new whereAmI(this.myAgent));
      } catch (FIPAException e) {
        e.printStackTrace();
      }
    }
  }

  static class whereAmI extends OneShotBehaviour {
    public whereAmI(Agent agent) {
      super(agent);
    }

    @Override
    public void action() {
      Location position = (Location) ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
      Behaviour wasat = new wasat(this.myAgent, 1000, position);
      Behaviour walker = new randomWalk(this.myAgent);
      this.myAgent.addBehaviour(wasat);
      this.myAgent.addBehaviour(walker);
      this.myAgent.addBehaviour(new watcher(this.myAgent, new ArrayList<>(Arrays.asList(walker))));
    }
  }

  static class randomWalk extends Behaviour {
    private final String targetPosition;

    public randomWalk(Agent agent) {
      super(agent);
      this.targetPosition = null;
    }

    public randomWalk(Agent agent, String position) {
      super(agent);
      System.out.println("Target position: " + position);
      this.targetPosition = position.trim();
    }

    @Override
    public void action() {
      List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
          .observe();
      int moveId = 0;
      while (true) {
        Random r = new Random();
        moveId = 1 + r.nextInt(lobs.size() - 1);
        boolean safe = true;
        for (Couple<Observation, Integer> obs : lobs.get(moveId).getRight()) {
          if (obs.getLeft().equals(Observation.WIND)) {
            safe = false;
            break;
          }
        }
        if (safe)
          break;
      }
      ((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
    }

    @Override
    public boolean done() {
      if (this.targetPosition == null)
        return false;
      else {
        return ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().toString().equals(this.targetPosition);
      }
    }
  }

  static class watcher extends CyclicBehaviour {
    private final ArrayList<Behaviour> victim;

    public watcher(Agent agent, ArrayList<Behaviour> victims) {
      super(agent);
      this.victim = victims;
    }

    @Override
    public void action() {
      ACLMessage msg = this.myAgent.receive();
      if (msg == null) {
        block();
        return;
      }

      if (msg.getPerformative() == ACLMessage.INFORM) {
        String content = msg.getContent();
        if (content.contains("Position:") && ((AgentA) this.myAgent).myPosition == null) {
          System.out.println("AgentA received: " + msg.getContent());
          AgentA agent = (AgentA) this.myAgent;
          agent.myPosition = content.split(" ")[1];
        } 
        if (content.contains("ACK:") && ((AgentA) this.myAgent).myPosition != null) {
          System.out.println("ACK received, stopping behaviour");
          this.myAgent.addBehaviour(new randomWalk(this.myAgent, ((AgentA) this.myAgent).myPosition));
          for (Behaviour b : this.victim) {
            this.myAgent.removeBehaviour(b);
          }
          this.myAgent.removeBehaviour(this);
        }
      }
    }
  }

  static class wasat extends TickerBehaviour {
    private final Location initialPosition;
    private DFAgentDescription to = null;

    public wasat(Agent agent, long period, Location position) {
      super(agent, period);
      this.initialPosition = position;
    }

    @Override
    public void onTick() {
      DFAgentDescription template = new DFAgentDescription();
      ServiceDescription sd = new ServiceDescription();
      sd.setType("Explo");
      template.addServices(sd);
      try {
        DFAgentDescription[] result = DFService.search(this.myAgent, template);
        if (this.to == null) {
          for (int i = 0; i < result.length; ++i) {
            if (result[i].getName().getName().contains("AgentB")) {
              this.to = result[i];
              break;
            }
          }
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(this.to.getName());
        String content = "Position: " + this.initialPosition;
        if (((AgentA) this.myAgent).myPosition != null) {
          content += " ACK: " + ((AgentA) this.myAgent).myPosition;
        }
        msg.setContent(content);
        msg.setSender(this.myAgent.getAID());
        AbstractDedaleAgent agent = (AbstractDedaleAgent) this.myAgent;
        agent.sendMessage(msg);
      } catch (FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

  /**
   * This method is automatically called when "agent".start() is executed.
   * Consider that Agent is launched for the first time.
   * 1) set the agent attributes
   * 2) add the behaviours
   */
  protected void setup() {
    super.setup();
    // use them as parameters for your behaviours is you want
    List<Behaviour> lb = new ArrayList<>();

    // ADD the initial behaviours
    lb.add(new service(this));

    // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
    addBehaviour(new startMyBehaviours(this, lb));
  }

  public String getTargetPosition() {
    return this.myPosition;
  }

  /**
   * This method is automatically called after doDelete()
   */
  protected void takeDown() {
    super.takeDown();
  }

  /**
   * This method is automatically called before migration.
   * You can add here all the saving you need
   */
  protected void beforeMove() {
    super.beforeMove();
  }

  /**
   * This method is automatically called after migration to reload.
   * You can add here all the info regarding the state you want your agent to
   * restart from
   */
  protected void afterMove() {
    super.afterMove();
  }
}
