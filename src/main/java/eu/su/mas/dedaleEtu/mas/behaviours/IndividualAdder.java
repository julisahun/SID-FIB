package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.AgentTest;
import jade.core.behaviours.OneShotBehaviour;
import org.apache.jena.ontology.OntModel;

public class IndividualAdder extends OneShotBehaviour {
  final String BASE_URI = "http://www.semanticweb.org/juli/ontologies/2023/3/untitled-ontology-2#";
  final String FILE_NAME = "problema2";
  private String className;
  private String instance;

  public IndividualAdder(Agent a, String className, String instance) {
    super(a);
    this.className = className;
    this.instance = instance;
  }

  @Override
  public void action() {
    AgentTest agent = (AgentTest) this.myAgent;
    OntModel ont = (OntModel) agent.getOntology().getValue();
    ont.createIndividual(BASE_URI + instance, ont.getOntClass(BASE_URI + className));
  }
}