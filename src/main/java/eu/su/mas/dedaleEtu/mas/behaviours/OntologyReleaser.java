package eu.su.mas.dedaleEtu.mas.behaviours;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import eu.su.mas.dedaleEtu.mas.behaviours.OntologyLoader;

import java.net.URL;

public class OntologyReleaser extends OneShotBehaviour {
    final String FILE_NAME = "problema2";
    public OntologyReleaser(Agent a) {
        super(a);
    }
    
    private void releaseOntology() throws FileNotFoundException {
      AgentTest agent = (AgentTest) this.myAgent;
      OntModel ont = (OntModel) agent.getOntology().getValue();
      if (!ont.isClosed()) {
          String sep = File.separator;
          Path resourcePath = Paths.get(this.getClass().getResource(sep).getPath());
          ont.write(new FileOutputStream(resourcePath + sep + FILE_NAME +
                  "-modified.owl", false));
          ont.close();
      }
    }

    @Override
    public void action() {
      try {
        this.releaseOntology();
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      System.out.println("releasing ontology... from behavior");
    }
}
