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
import eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours.IndividualAdder;
import eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours.OntologyLoader;
import eu.su.mas.dedaleEtu.mas.behaviours.ontologyBehaviours.OntologyReleaser;
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
    private Belief ontology;

    protected void setup() {
        super.setup();
        String name = this.getLocalName();
        // use them as parameters for your behaviours is you want
        Behaviour ontologyLoader = new OntologyLoader(this);

        String type = this.getAgentType();
        Behaviour addMyself = new IndividualAdder(this, type, name);

        List<Behaviour> callbacks = new ArrayList<>();
        callbacks.add(new OntologyReleaser(this));
        Behaviour walker = new ExplorerAndUpdaterBehaviour(this, null, callbacks);

        List<Behaviour> lb = new ArrayList<>();
        lb.add(ontologyLoader);
        lb.add(addMyself);
        lb.add(walker);

        // MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
        addBehaviour(new startMyBehaviours(this, lb));
    }

    private String getAgentType() {
        String field = this.getArguments()[0].toString().split(";")[1].trim();
        String rawType = field.split(" ")[1].trim();

        if (rawType.equals("AgentExplo"))
            return "Explorer";
        if (rawType.equals("AgentCollect"))
            return "Collector";
        if (rawType.equals("AgentTanker"))
            return "Tanker";
        return "Agent";
    }

    public void setOntology(Belief ontology) {
        this.ontology = ontology;
    }

    public Belief getOntology() {
        return ontology;
    }
}
