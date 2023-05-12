package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.BDIAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours.OntologyLoader;

import java.util.ArrayList;
import java.util.List;

public class AgentA extends AbstractDedaleAgent {

  protected void setup() {
    super.setup();
    // use them as parameters for your behaviours is you want
    List<Behaviour> lb = new ArrayList<>();

    // ADD the initial behaviours
    lb.add(new RegisterToDF(this, "AgentA", "dedale"));
    lb.add(new SendRE(this, "AgentB", "goto:46"));

    // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
    addBehaviour(new startMyBehaviours(this, lb));
  }
}
