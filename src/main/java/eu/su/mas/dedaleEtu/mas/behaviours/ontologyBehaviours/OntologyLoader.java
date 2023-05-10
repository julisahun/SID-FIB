package eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours;

import jade.core.Agent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.AgentTest;
import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.ONTOLOGY;
import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import jade.core.behaviours.OneShotBehaviour;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.net.URL;

public class OntologyLoader extends OneShotBehaviour {
    final String FILE_NAME = "problema22";

    public OntologyLoader(Agent a) {
        super(a);
    }

    private Model loadOntology() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        OntDocumentManager dm = model.getDocumentManager();
        URL fileAsResource = getClass().getClassLoader().getResource(FILE_NAME + ".owl");
        dm.addAltEntry(FILE_NAME, fileAsResource.toString());
        model.read(FILE_NAME);
        return model;
    }

    @Override
    public void action() {
        System.out.println("loading ontology... from behavior");
        Model ont = this.loadOntology();
        AgentTest agent = (AgentTest) this.myAgent;
        agent.setOntology(new TransientBelief(ONTOLOGY, ont));
    }
}
