package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MessageSender extends OneShotBehaviour {

    private final String message;

    private final String[] receivers;

    public MessageSender(Agent a, String message, String[] receivers) {
        super(a);
        this.message = message;
        this.receivers = receivers;
    }

    @Override
    public void action() {
        System.out.println(this.message);
    }

    private SituatedAgent getLocalAgent() {
        return (SituatedAgent) this.myAgent;
    }
}
