package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.situated.behaviours.ontologyBehaviours;

import jade.core.Agent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.AgentTest;
import jade.core.behaviours.OneShotBehaviour;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;

public class PropertyAdder extends OneShotBehaviour {
  final String BASE_URI = "http://www.semanticweb.org/juli/ontologies/2023/3/untitled-ontology-2#";
  private String property;
  private String to;
  private String from;
  private boolean generic;

  public PropertyAdder(Agent a, String from, String property, String to) {
    super(a);
    this.property = property;
    this.to = to;
    this.from = from;
    this.generic = true;
  }

  public PropertyAdder(Agent a, String from, String property, String to, boolean generic) {
    super(a);
    this.property = property;
    this.to = to;
    this.generic = generic;
    this.from = from;
  }

  @Override
  public void action() {
    AgentTest agent = (AgentTest) this.myAgent;
    OntModel ont = (OntModel) agent.getOntology().getValue();
    Individual target = ont.getIndividual(BASE_URI + this.from);
    Property nameProperty = ont.getProperty(BASE_URI + property);
    if (!generic) {
      Individual to = ont.getIndividual(BASE_URI + this.to);
      target.addProperty(nameProperty, to);
    } else {
      target.addProperty(nameProperty, this.to);
    }
  }
}