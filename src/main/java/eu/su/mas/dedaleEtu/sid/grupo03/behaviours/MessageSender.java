package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageSender extends OneShotBehaviour {

    private final String message;
    private int performative = ACLMessage.INFORM;
    private final String[] receivers;

    public MessageSender(Agent a, String message, String[] receivers) {
        super(a);
        this.message = message;
        this.receivers = receivers;
    }

    public MessageSender(Agent a, String message) {
        super(a);
        this.message = message;
        String masterName = ((SituatedAgent03) a).master;
        this.receivers = new String[] { masterName };
    }

    public MessageSender(Agent a, int performative, String message) {
        super(a);
        this.message = message;
        String masterName = ((SituatedAgent03) a).master;
        this.receivers = new String[] { masterName };
        this.performative = performative;
    }

    public MessageSender(Agent a, int performative, String message, String[] receivers) {
        super(a);
        this.message = message;
        this.receivers = receivers;
        this.performative = performative;
    }

    @Override
    public void action() {
        for (String receiver : this.receivers) {
            if (receiver == null)
                continue;
            Utils.sendMessage(this.myAgent, this.performative, this.message, receiver);
        }
    }
}
