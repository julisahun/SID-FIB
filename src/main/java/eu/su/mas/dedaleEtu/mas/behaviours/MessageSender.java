package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class MessageSender extends SimpleBehaviour {

    private final String message;

    private final String[] receivers;
    private final boolean waitForReply;
    private final String guardClause;
    public MessageSender(Agent a, String message, String[] receivers, String guardClause) {
     super(a);
     this.message = message;
     this.receivers = receivers;
     this.guardClause = guardClause;
     this.waitForReply = guardClause != null;
    }

    public MessageSender(Agent a, String[] receivers, String message) {
        super(a);
        this.message = message;
        this.receivers = receivers;
        this.guardClause = null;
        this.waitForReply = false;
    }

    @Override
    public void action() {
        //send message to all receivers
    }

    @Override
    public boolean done() {
        return !this.waitForReply || this.getLocalAgent().getResponse(this.guardClause);
    }

    private SituatedAgent getLocalAgent() {
        return (SituatedAgent) this.myAgent;
    }
}
