package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import eu.su.mas.dedaleEtu.mas.behaviours.*;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.ONTOLOGY;

public class AgentTest extends AbstractDedaleAgent {
    final String BASE_URI = "http://www.semanticweb.org/juli/ontologies/2023/3/untitled-ontology-2#";
    final String FILE_NAME = "problema2.rdf";
    private Belief ontology;

    protected void setup() {
        super.setup();
        String name = this.getLocalName();
        //use them as parameters for your behaviours is you want
        List<Behaviour> lb = new ArrayList<>();

        lb.add(new OntologyLoader(this));

        lb.add(new IndividualAdder(this, "Agent", name));
        List<Behaviour> callbacks = new ArrayList<>();
        callbacks.add(new OntologyReleaser(this));
        lb.add(new ExplorerAndUpdaterBehaviour(this, null, callbacks));
        // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
        addBehaviour(new startMyBehaviours(this, lb));
    }

    public void setOntology(Belief ontology) {
        this.ontology = ontology;
    }

    public Belief getOntology() {
        return ontology;
    }
}
