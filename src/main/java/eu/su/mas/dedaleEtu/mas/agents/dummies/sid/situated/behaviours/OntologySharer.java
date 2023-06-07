package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

//tried Cyclic behaviour but it was using too much CPU, so I switched to TickerBehaviour
public class OntologySharer extends TickerBehaviour {

  private final ACLMessage message;

  public OntologySharer(Agent a, String message) {
    super(a, 100);

    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setSender(this.myAgent.getAID());
    msg.setContent(message);
    msg.setProtocol("SHARE-ONTO");
    msg.setOntology("polydama-mapstate");

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    templateSd.setType("dedale");
    template.addServices(templateSd);
    DFAgentDescription[] results;
    try {
      results = DFService.search(this.myAgent, template);
      for (DFAgentDescription agent : results) {
        AID provider = agent.getName();
        msg.addReceiver(provider);
      }
    } catch (FIPAException e) {
      // do nothing
    } finally {
      this.message = msg;
    }
  }

  public void updateOntology(String ontology) {
    this.message.setContent(ontology);
  }

  @Override
  public void onTick() {
    ((AbstractDedaleAgent) this.myAgent).sendMessage(this.message);
  }
}
