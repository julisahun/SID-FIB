package eu.su.mas.dedaleEtu.sid.grupo03.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.sid.grupo03.SituatedAgent03;
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

  private ACLMessage message;
  String ontology;
  Integer ticks = 0;

  public OntologySharer(Agent a, String ontology) {
    super(a, 100);
    this.ontology = ontology;
    this.refreshMessage();

  }

  public void updateOntology(String ontology) {
    this.ontology = ontology;
    this.refreshMessage();
  }

  private void refreshMessage() {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.setSender(this.myAgent.getAID());
    msg.setContent(this.ontology);
    msg.setProtocol("SHARE-ONTO");
    msg.setOntology("polydama-mapstate");

    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription templateSd = new ServiceDescription();
    template.addServices(templateSd);
    DFAgentDescription[] results;
    try {
      results = DFService.search(this.myAgent, template);
      for (DFAgentDescription agent : results) {
        Boolean isMySelf = agent.getName().getLocalName().equals(this.myAgent.getLocalName());
        Boolean isMyMaster = agent.getName().getLocalName().equals(((SituatedAgent03) this.myAgent).master);
        if (isMySelf || isMyMaster)
          // avoid adding itself or my master to the receivers
          continue;
        AID provider = agent.getName();
        msg.addReceiver(provider);
      }
    } catch (FIPAException e) {
      // do nothing
    } finally {
      this.message = msg;
    }
  }

  @Override
  public void onTick() {
    ((AbstractDedaleAgent) this.myAgent).sendMessage(this.message);
  }
}
