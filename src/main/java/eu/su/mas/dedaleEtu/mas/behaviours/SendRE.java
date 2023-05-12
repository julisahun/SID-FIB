package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPANames;


public class SendRE extends OneShotBehaviour{

  private String receiver;
  private String content;
  public SendRE(Agent a, String receiver, String content) {
    super();
    this.receiver = receiver;
    this.content = content;
  }


  private AID find() {
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("dedale");
    template.addServices(templateSd);
    try {
      DFAgentDescription[] result = jade.domain.DFService.search(this.myAgent, template);
      for (DFAgentDescription agent : result) {
        if (agent.getName().getLocalName().equals(this.receiver)) {
          return agent.getName();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void action() {
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.setSender(this.myAgent.getAID());
    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    msg.addReceiver(this.find());
    msg.setContent(this.content);
    this.myAgent.doWait(20000);
    ((AbstractDedaleAgent) this.myAgent).addBehaviour(new AchiveREInitiator(myAgent, msg));
  }
}
