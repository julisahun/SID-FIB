package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.BDIAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours.OntologyLoader;

import jade.core.behaviours.Behaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.BehaviourUtils;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.proto.AchieveREResponder;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;


import java.util.ArrayList;
import java.util.List;

public class AgentB extends AbstractDedaleAgent {

  protected void setup() {
    super.setup();
    // use them as parameters for your behaviours is you want
    List<Behaviour> lb = new ArrayList<>();

    // ADD the initial behaviours
    lb.add(new RegisterToDF(this, "AgentB", "dedale"));
    lb.add(responder());
    // lb.add(new WalkTo(this, "43", ""));


    // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
    addBehaviour(new startMyBehaviours(this, lb));
  }
  
  private Behaviour responder () {
    MessageTemplate template = MessageTemplate.and(
    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
    MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );

    Behaviour res = new  AchieveREResponder(this, template) {
			protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {				
					// We agree to perform the action. Note that in the FIPA-Request
					// protocol the AGREE message is optional. Return null if you
					// don't want to send it.
        ACLMessage agree = request.createReply();
        agree.setPerformative(ACLMessage.AGREE);
        String content = request.getContent();
        String startingPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
        Behaviour one = new WalkTo(this.myAgent, content.split(":")[1], "");
        Behaviour two = new WalkTo(this.myAgent, startingPosition, "");
        Behaviour three = new OneShotBehaviour(this.myAgent) {
          public void action() {
            ACLMessage msg = request.createReply(ACLMessage.INFORM);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.setContent("done");

            ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
          }
        };
        List<Behaviour> callbacks = new ArrayList<>();
        callbacks.add(one);
        callbacks.add(two);
        callbacks.add(three);
        this.myAgent.addBehaviour(new Composer(this.myAgent, callbacks));
        return agree;
			}

      protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				if (false) {
					System.out.println("Agent "+getLocalName()+": Action successfully performed");
					ACLMessage inform = request.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}
				else {
					System.out.println("Agent "+getLocalName()+": Action failed");
					throw new FailureException("unexpected-error");
				}	
			}
            
		};
    return res;
  }
}
