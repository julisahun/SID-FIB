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
import java.util.Random;

public class AgentA extends AbstractDedaleAgent {

    class service extends OneShotBehaviour {
        public service(AbstractDedaleAgent agent) { super(agent); }

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
            this.myAgent.addBehaviour(wasat);
            this.myAgent.addBehaviour(new watcher(this.myAgent, wasat));
        }
    }

    static class randomWalk extends Behaviour {
        private final String finalPosition;
        public randomWalk(Agent agent, String position) {
            super(agent);
            this.finalPosition = position.trim();
        }

        @Override
        public void action() {
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();
            int moveId = 0;
            while(true) {
                Random r = new Random();
                moveId = 1 + r.nextInt(lobs.size() - 1);
                boolean safe = true;
                for (Couple<Observation, Integer> obs : lobs.get(moveId).getRight()) {
                    if (obs.getLeft().equals(Observation.WIND)) {
                        safe = false;
                        break;
                    }
                    else {
                        System.out.println(obs.getLeft().toString());
                    }                      
                }
                if (safe) break;
            }

            ((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
        }


        @Override
        public boolean done() {
            return ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().toString().equals(this.finalPosition);
        }

        public int onEnd() {
            System.out.println("AgentA reached final position");
            return 0;
        }
    }
    static class watcher extends CyclicBehaviour {
        private final Behaviour victim;
        public watcher(Agent agent, Behaviour victim){
            super(agent);
            this.victim = victim;
        }

        @Override
        public void action() {
            ACLMessage msg = this.myAgent.receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    String content = msg.getContent();
                    if (content.startsWith("Position:")) {
                        System.out.println("AgentAaa received: " + msg.getContent());
                        
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("ACK: OK");
                        this.myAgent.send(reply);
                        this.myAgent.addBehaviour(new randomWalk(this.myAgent, content.substring(9)));
                    } else if (content.startsWith("ACK:")) {
                        System.out.println("ACK received, stopping behaviour");
                        this.myAgent.removeBehaviour(this.victim);

                    }                                    
                }
                else {
                    AbstractDedaleAgent agent = (AbstractDedaleAgent) this.myAgent;
                    List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = agent.observe();
                    lobs.forEach(obs -> {
                        System.out.println(obs.getLeft().toString() + " " + obs.getRight().toString());
                    });
                    System.out.println("AgentA received nothing");
                    block();
                }
            }
        }
    }

    static class wasat extends TickerBehaviour {
        private final Location initialPosition;
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
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println("Found the following agents:");
                DFAgentDescription target = null;
                for (int i = 0; i < result.length; ++i) {
                    System.out.println(result[i].getName().getName());
                    if (result[i].getName().getName().contains("AgentB")) {
                        target = result[i];
                        break;

                    }
                }
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(target.getName());
                msg.setContent("Position: " + initialPosition);
                myAgent.send(msg);
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
        //use them as parameters for your behaviours is you want
        List<Behaviour> lb = new ArrayList<>();


        // ADD the initial behaviours
        lb.add(new service(this));

        // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
        addBehaviour(new startMyBehaviours(this, lb));
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
     * You can add here all the info regarding the state you want your agent to restart from
     */
    protected void afterMove() {
        super.afterMove();
    }
}
