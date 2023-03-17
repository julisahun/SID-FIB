package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class LabCollectorAgent extends AbstractDedaleAgent {

    class service extends OneShotBehaviour {
        public service(Agent agent) { super(agent); }

        @Override
        public void action() {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("collector");
            sd.setName("lab2");
            dfd.addServices(sd);
            try {
                DFService.register(myAgent, dfd);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    class read extends TickerBehaviour {
        public read(Agent agent, long period) { super(agent, period); }

        @Override
        public void onTick() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("I am " + myAgent.getLocalName() + " and I received " + msg.getContent());
            } else {
                System.out.println("I am " + myAgent.getLocalName() + " and I received nothing");
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
        lb.add(new read(this, 1000));


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
