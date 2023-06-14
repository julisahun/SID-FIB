package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import org.json.JSONObject;

import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageSender extends OneShotBehaviour {

    private JSONObject message;
    private int performative = ACLMessage.INFORM;
    private final String[] receivers;

    public MessageSender(SituatedAgent03 agent, JSONObject message, String[] receivers) {
        super(agent);
        this.message = message;
        this.receivers = receivers;
    }

    public MessageSender(SituatedAgent03 agent, JSONObject message) {
        super(agent);
        this.message = message;
        String masterName = agent.master;
        this.receivers = new String[] { masterName };
    }

    public MessageSender(SituatedAgent03 agent, int performative, JSONObject message) {
        super(agent);
        this.message = message;
        String masterName = agent.master;
        this.receivers = new String[] { masterName };
        this.performative = performative;
    }

    public MessageSender(SituatedAgent03 agent, int performative, JSONObject message, String[] receivers) {
        super(agent);
        this.message = message;
        this.receivers = receivers;
        this.performative = performative;
    }

    @Override
    public void action() {
        message.put("position", ((SituatedAgent03) this.myAgent).getCurrentPosition().getLocationId());
        for (String receiver : this.receivers) {
            if (receiver == null)
                continue;
            Utils.sendMessage(this.myAgent, this.performative, this.message.toString(), receiver);
        }
    }
}
