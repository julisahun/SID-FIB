package eu.su.mas.dedaleEtu.mas;

import org.apache.commons.compress.utils.Lists;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.ResultBinding;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class JenaTester {
    private static final String PIZZA_BASE_URI = "http://www.co-ode.org/ontologies/pizza/pizza.owl";

    OntModel model;
    String JENAPath;
    String OntologyFile;
    String NamingContext;
    OntDocumentManager dm;

    public JenaTester(String _File, String _NamingContext) {
        this.OntologyFile = _File;
        this.NamingContext = _NamingContext;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String File = "pizza.owl";

        System.out.println("----------------Starting program -------------");

        JenaTester tester = new JenaTester(File, PIZZA_BASE_URI);

        tester.loadOntology();
        tester.getClasses();
        tester.getIndividuals();
        tester.getIndividualsByClass();
        tester.getPropertiesByClass();
        tester.runSparqlQueryDataProperty();
        tester.runSparqlQueryObjectProperty();
        tester.runSparqlQueryModify();
        tester.testEquivalentClass();
        tester.exportStatement();
        tester.releaseOntology();

        System.out.println("--------- Program terminated --------------------");
    }

    public void loadOntology() {
        System.out.println("\n\n· Loading Ontology");
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        dm = model.getDocumentManager();

        // Two options to import: absolute path in file system or classpath URL
        //dm.addAltEntry("", "file:/home/juli/IdeaProjects/dedale-etu1/src/main/resources/pizza.owl");
        URL fileAsResource = getClass().getClassLoader().getResource("pizza.owl");
        dm.addAltEntry(NamingContext, fileAsResource.toString());
        model.read(NamingContext);
    }

    public void releaseOntology() throws FileNotFoundException {
        System.out.println("\n\n· Releasing Ontology");
        if (!model.isClosed()) {
            String sep = File.separator;
            Path resourcePath =
                    Paths.get(this.getClass().getResource(sep).getPath());
            model.write(new FileOutputStream(resourcePath + sep + "pizza" +
                    "-modified.owl", false));
            model.close();
        }
    }

    public void getIndividuals() {
        System.out.println("\n\n· Listing all individuals");
        for (Iterator<Individual> i = model.listIndividuals(); i.hasNext(); ) {
            Individual dummy = i.next();
            System.out.println("Ontology has individual: ");
            System.out.println("   " + dummy);
            Property nameProperty = model.getProperty(PIZZA_BASE_URI + "#hasPizzaName");
            RDFNode nameValue = dummy.getPropertyValue(nameProperty);
            System.out.println("   hasPizzaName = " + nameValue);
        }
    }

    public void getIndividualsByClass() {
        System.out.println("\n\n· Listing individuals per class");
        Iterator<OntClass> classesIt = model.listNamedClasses();
        while (classesIt.hasNext()) {
            OntClass actual = classesIt.next();
            System.out.println("Class: '" + actual.getURI() + "' has individuals:");
            OntClass pizzaClass = model.getOntClass(actual.getURI());
            for (Iterator<Individual> i = model.listIndividuals(pizzaClass); i.hasNext(); ) {
                System.out.println("    · " + i.next());
            }
        }
    }

    public void getPropertiesByClass() {
        System.out.println("\n\n· Listing properties per class");

        // All properties for the "Pizza Four Seasons" class

        OntClass pizza = model.getOntClass(PIZZA_BASE_URI + "#FourSeasons");
        System.out.println("Class: '" + pizza.getURI() + "' has properties:");
        OntClass pizzaClass = model.getOntClass(pizza.getURI());
        Iterator<OntProperty> itProperties = pizzaClass.listDeclaredProperties();

        while (itProperties.hasNext()) {
            OntProperty property = itProperties.next();
            System.out.println("    · Name :" + property.getLocalName());
            System.out.println("        · Domain :" + property.getDomain());
            System.out.println("        · Range :" + property.getRange());
            System.out.println("        · Inverse :" + property.getInverse());
            System.out.println("        · IsData :" + property.isDatatypeProperty());
            System.out.println("        · IsFunctional :" + property.isFunctionalProperty());
            System.out.println("        · IsObject :" + property.isObjectProperty());
            System.out.println("        · IsSymetric :" + property.isSymmetricProperty());
            System.out.println("        · IsTransitive :" + property.isTransitiveProperty());
        }
    }

    private void addInstances(String classUri, String className) {
        System.out.println("· Adding instance to '" + className + "'");
        OntClass pizzaClass = model.getOntClass(classUri);
        Individual particularPizza = pizzaClass.createIndividual(PIZZA_BASE_URI + "#" + className + "Example");
        Property nameProperty = model.getProperty(PIZZA_BASE_URI + "#hasPizzaName");
        particularPizza.addProperty(nameProperty, "A yummy " + className);
    }

    public void getClasses() {
        System.out.println("\n\n· Listing classes in ontology and add instances");
        //List of ontology classes
        Iterator<OntClass> classesIt = model.listNamedClasses();
        List<OntClass> classes = Lists.newArrayList(classesIt);

        OntClass pizza = model.getOntClass(PIZZA_BASE_URI + "#NamedPizza");
        OntClass nothing = model.getOntClass("http://www.w3.org/2002/07/owl#Nothing");

        for (OntClass actual : classes) {
            System.out.println("Ontology has class: " + actual.getURI());
            // Be careful! owl#Nothing is the subclass of all classes
            if (actual.hasSuperClass(pizza) && actual != pizza && actual != nothing) {
                addInstances(actual.getURI(), actual.getLocalName());
            }
        }
    }

    public void runSparqlQueryDataProperty() {
        System.out.println("\n\n· Running SPARQL to query a data property");

        // "Pizza instances and their names"
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX pizza: <" + PIZZA_BASE_URI + "#> " +
                "SELECT DISTINCT ?PizzaIndividual ?PizzaName where {" +
                "  ?PizzaIndividual a ?PizzaClass. " +
                "  ?PizzaClass rdfs:subClassOf pizza:Pizza. " +
                "  ?PizzaIndividual pizza:hasPizzaName ?PizzaName" +
                "}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            Object Pizza = res.get("PizzaIndividual");
            Object PizzaName = res.get("PizzaName");
            System.out.println("Pizza = " + Pizza + " <-> " + PizzaName);
        }
        qe.close();
    }

    public void runSparqlQueryObjectProperty() {
        System.out.println("\n\n· Running SPARQL to query an object property");

        // "Pizzas and country of origin when the country of origin is America"
        String queryStringRegExp = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX pizza: <" + PIZZA_BASE_URI + "#> " +
                "SELECT ?PizzaIndividual ?PizzaClass where {" +
                "  ?PizzaIndividual a ?PizzaClass. " +
                "  ?PizzaClass rdfs:subClassOf pizza:Pizza. " +
                "  ?PizzaIndividual pizza:hasCountryOfOrigin <" + PIZZA_BASE_URI + "#America>." +
                "}";

        Query query = QueryFactory.create(queryStringRegExp);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            ResultBinding res = (ResultBinding) results.next();
            Object Pizza = res.get("PizzaIndividual");
            Object PizzaClass = res.get("PizzaClass");
            System.out.println("Pizza = " + Pizza + " <-> " + PizzaClass);
        }
        qe.close();
    }

    public void runSparqlQueryModify() {
        System.out.println("\n\n· Running SPARQL to query the ontology and modify");

        // "Pizza instances and, optionally, whether they have been eaten"
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX pizza: <" + PIZZA_BASE_URI + "#> " +
                "SELECT ?Pizza ?Eaten where {" +
                "  ?Pizza a ?y. " +
                "  ?y rdfs:subClassOf pizza:Pizza." +
                "  OPTIONAL {?Pizza pizza:Eaten ?Eaten}" +
                "}";

        Query query = QueryFactory.create(queryString);

        QueryExecution qe = QueryExecutionFactory.create(query, model);
        List<QuerySolution> results = Lists.newArrayList(qe.execSelect());

        for (QuerySolution res : results) {
            Object Pizza = res.get("Pizza");
            Object Eaten = res.get("Eaten");
            if (Eaten == null) {
                System.out.println("Pizza = " + Pizza + " <-> false");
                Individual actualPizza = model.getIndividual(Pizza.toString());
                Property eatenProperty = model.getProperty(PIZZA_BASE_URI + "#Eaten");
                Literal rdfBoolean = model.createTypedLiteral(Boolean.valueOf("true"));
                actualPizza.addProperty(eatenProperty, rdfBoolean);
            } else {
                System.out.println("Pizza = " + Pizza + " <-> " + Eaten);
            }
        }
        qe.close();
    }

    private void testEquivalentClass() {
        System.out.println("\n\n· Test equivalent classes inference");

        // We add Italy as the country of origin of FourSeasons
        Individual instance = model.getIndividual(PIZZA_BASE_URI + "#FourSeasonsExample");
        Individual italy = model.getIndividual(PIZZA_BASE_URI + "#Italy");
        Property hasCountryOfOrigin = model.getObjectProperty(PIZZA_BASE_URI + "#hasCountryOfOrigin");
        instance.addProperty(hasCountryOfOrigin, italy);

        // We check the inferences
        boolean isRealItalian = instance.hasOntClass(PIZZA_BASE_URI + "#RealItalianPizza");
        boolean isSpicy = instance.hasOntClass(PIZZA_BASE_URI + "#SpicyPizza");
        System.out.println("FourSeasonsInstance classifies as RealItalianPizza?: " + isRealItalian);
        System.out.println("FourSeasonsInstance classifies as SpicyPizza?: " + isSpicy);
    }

    private void exportStatement() {
        System.out.println("\n\n· Exporting Statement");

        // Create the triplet
        Individual france = model.getIndividual(PIZZA_BASE_URI + "#France");
        Property isA = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        OntClass country = model.getOntClass(PIZZA_BASE_URI + "#Country");
        Statement statement = new StatementImpl(france, isA, country);

        // Create an empty model that will hold the triplet
        Model tempModel = ModelFactory.createDefaultModel();
        tempModel.add(statement);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFDataMgr.write(output, tempModel, Lang.NTRIPLES);
        System.out.println("Serialization: " + output);

        // Parse the string back into a model and check the statements it contains
        System.out.println("\n· Reading Statements");
        Model readModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(readModel, new ByteArrayInputStream(output.toByteArray()), Lang.NTRIPLES);
        StmtIterator statements = readModel.listStatements();
        while (statements.hasNext()) {
            System.out.println(statements.next().toString());
        }
    }
}