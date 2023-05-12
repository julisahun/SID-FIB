package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class AchiveREInitiator extends AchieveREInitiator {

  public AchiveREInitiator(Agent a, ACLMessage msg) {
    super(a, msg);
    System.out.println(msg.getContent());
  }

  @Override
  protected void handleInform(ACLMessage inform) {
    System.out.println("Agent " + this.myAgent.getLocalName() + " received the following message: " + inform.getContent());
  }

  @Override
  protected void handleRefuse(ACLMessage refuse) {
    System.out.println("Agent " + this.myAgent.getLocalName() + ": Action refused");
  }

  @Override
  protected void handleFailure(ACLMessage failure) {
    if (failure.getSender().equals(myAgent.getAMS())) {
      // FAILURE notification from the JADE runtime: the receiver
      // does not exist
      System.out.println("Responder does not exist");
    } else {
      System.out.println("Agent " + failure.getSender().getName() + " failed to perform the requested action");
    }
  }
}
