package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class RegisterToDF extends OneShotBehaviour {
  private String name;
  private String type;

  public RegisterToDF(Agent a, String name, String type) {
    super(a);
    this.name = name;
    this.type = type;
    System.out.println("registering " + name + " to DF" + " of type " + type);
  }

  @Override
  public void action() {
    System.out.println("Registering situated agent to DF " + this.myAgent.getLocalName());
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(this.myAgent.getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setName(this.name);
    sd.setType(this.type);
    sd.addOntologies("polydama-mapstate");
    dfd.addServices(sd);
    try {
      DFService.register(this.myAgent, dfd);
      System.out.println("Situated agent " + this.myAgent.getLocalName() + " registered!");
    } catch (FIPAException e) {
      e.printStackTrace();
    }
  }
}
