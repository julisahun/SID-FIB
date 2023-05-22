package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.agents;

import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientPredicate;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.*;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.reasoning.*;
import dataStructures.tuple.Couple;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet.GoalDescription;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SPARQLGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.CommandSentPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.KeepMailboxEmptyPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.RegisterPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.SituatedListeningPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import eu.su.mas.dedaleEtu.mas.knowledge.Node;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.net.URL;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

public class BDIAgent extends SingleCapabilityAgent {

    private ArrayList<String> messages = new ArrayList<>();

    public BDIAgent() {
        // Create initial beliefs
        Belief<String, Boolean> iAmRegistered = new TransientPredicate<String>(I_AM_REGISTERED, false);
        Belief<String, MapaModel> ontology = new TransientBelief<String, MapaModel>(ONTOLOGY, Utils.loadOntology());
        Belief<String, Boolean> isSlaveAlive = new TransientPredicate<String>(IS_SLAVE_ALIVE, false);
        Belief<String, HashMap<String, Node>> map = new TransientBelief<String, HashMap<String, Node>>(
                MAP, new HashMap<>());
        Belief<String, Boolean> isFullExplored = new TransientPredicate<String>(IS_FULL_EXPLORED, false);
        Belief<String, Boolean> commandSent = new TransientPredicate<String>(COMMAND_SENT, false);
        Belief<String, HashSet> rejectedNodes = new TransientBelief<String, HashSet>(REJECTED_NODES, new HashSet<>());
        Belief<String, Boolean> situatedPinged = new TransientPredicate<String>(SITUATED_PINGED, false);

        // Add initial desires
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        Goal findSituatedGoal = new SPARQLGoal<String>(ONTOLOGY, QUERY_SITUATED_AGENT);
        Goal situatedListeningGoal = new PredicateGoal<String>(IS_SLAVE_ALIVE, true);
        Goal commandSentGoal = new PredicateGoal<String>(COMMAND_SENT, true);
        Goal situatedPingedGoal = new PredicateGoal<String>(SITUATED_PINGED, true);

        addGoal(registerGoal);
        addGoal(findSituatedGoal);
        addGoal(situatedListeningGoal);
        addGoal(commandSentGoal);
        addGoal(situatedPingedGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate commandSentTemplate = matchesGoal(commandSentGoal);
        GoalTemplate situatedPingedTemplate = matchesGoal(situatedPingedGoal);

        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan keepMailboxEmptyPlan = new DefaultPlan(MessageTemplate.MatchAll(),
                KeepMailboxEmptyPlanBody.class);
        Plan situatedListeningPlan = new DefaultPlan(
                situatedPingedTemplate, SituatedListeningPlanBody.class);
        Plan commandSentPlan = new DefaultPlan(
                commandSentTemplate, CommandSentPlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(keepMailboxEmptyPlan);
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(situatedListeningPlan);
        getCapability().getPlanLibrary().addPlan(commandSentPlan);

        // Init belief base
        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(isSlaveAlive);
        getCapability().getBeliefBase().addBelief(map);
        getCapability().getBeliefBase().addBelief(isFullExplored);
        getCapability().getBeliefBase().addBelief(commandSent);
        getCapability().getBeliefBase().addBelief(rejectedNodes);
        getCapability().getBeliefBase().addBelief(situatedPinged);

        // Add a goal listener to track events
        enableGoalMonitoring();

        // Override BDI cycle meta-functions, if needed
        overrideBeliefRevisionStrategy();
        overrideOptionGenerationFunction();
        overrideDeliberationFunction();
        overridePlanSelectionStrategy();
    }

    private void overrideBeliefRevisionStrategy() {
        this.getCapability().setBeliefRevisionStrategy(new DefaultBeliefRevisionStrategy() {
            @Override
            public void reviewBeliefs() {
                Boolean commandSent = (Boolean) getCapability().getBeliefBase().getBelief(COMMAND_SENT).getValue();
                if (!commandSent) {

                }

                Belief map = getCapability().getBeliefBase().getBelief(MAP);
                HashMap<String, Couple<Boolean, HashSet<String>>> mapValue = (HashMap<String, Couple<Boolean, HashSet<String>>>) map
                        .getValue();
                for (String key : mapValue.keySet()) {
                    Couple<Boolean, HashSet<String>> nodeInfo = mapValue.get(key);
                    Boolean status = nodeInfo.getLeft();
                    if (!status) {
                        return;
                    }
                }
                if (mapValue.size() == 0)
                    return;
                Belief isFullExplored = getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED);
                isFullExplored.setValue(true);

                // This method should check belief base consistency,
                // make new inferences, etc.
                // The default implementation does nothing
            }
        });
    }

    private void overrideOptionGenerationFunction() {
        this.getCapability().setOptionGenerationFunction(new DefaultOptionGenerationFunction() {
            @Override
            public void generateGoals(GoalUpdateSet agentGoalUpdateSet) {
                agentGoalUpdateSet.getCurrentGoals().forEach(goal -> {
                    System.out.println("Current goal: " + goal);
                });
                Boolean commandSent = (Boolean) getCapability().getBeliefBase().getBelief(COMMAND_SENT).getValue();
                Boolean isSlaveAlive = (Boolean) getCapability().getBeliefBase().getBelief(IS_SLAVE_ALIVE).getValue();
                Boolean isFinished = (Boolean) getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED).getValue();
                if (isSlaveAlive && !commandSent && agentGoalUpdateSet.getCurrentGoals().size() == 0 && !isFinished) {
                    Goal commandSentGoal = new PredicateGoal<String>(COMMAND_SENT, true);
                    GoalTemplate commandSentTemplate = matchesGoal(commandSentGoal);
                    Plan commandSentPlan = new DefaultPlan(
                            commandSentTemplate, CommandSentPlanBody.class);
                    getCapability().getPlanLibrary().addPlan(commandSentPlan);
                    agentGoalUpdateSet.generateGoal((commandSentGoal));
                }
            }
        });
    }

    public void addMessage(ACLMessage msg) {
        String content = msg.getContent();
        String sender = msg.getSender().getLocalName();
        int performative = msg.getPerformative();
        String timestamp = new Timestamp(System.currentTimeMillis()).toString();

        JSONObject body = new JSONObject();
        body.put("sender", sender);
        body.put("performative", performative);
        body.put("content", content);
        body.put("timestamp", timestamp);

        this.messages.add(body.toString());
    }

    public void printMessages() {
        for (String message : this.messages) {
            System.out.println(message);
        }
    }

    private void overrideDeliberationFunction() {
        this.setDeliberationFunction(new DefaultAgentDeliberationFunction() {
            @Override
            public Set<Goal> filter(Set<GoalDescription> agentGoals,
                    Map<Capability, Set<GoalDescription>> capabilityGoals) {
                Boolean iAmRegistered = (Boolean) getCapability().getBeliefBase().getBelief(I_AM_REGISTERED).getValue();
                Boolean isSlaveAlive = (Boolean) getCapability().getBeliefBase().getBelief(IS_SLAVE_ALIVE).getValue();
                Boolean pingSent = (Boolean) getCapability().getBeliefBase().getBelief(SITUATED_PINGED).getValue();
                Boolean commandSent = (Boolean) getCapability().getBeliefBase().getBelief(COMMAND_SENT).getValue();
                Boolean isFullExplored = (Boolean) getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED)
                        .getValue();
                Set<Goal> goals = new HashSet<>();
                for (GoalUpdateSet.GoalDescription goalDescription : agentGoals) {
                    Goal goal = goalDescription.getGoal();
                    if (goal.getClass().getName().contains("MessageGoal")) {
                        goals.add(goal);
                    }
                    if (goal.equals(new PredicateGoal<String>(I_AM_REGISTERED, true))) {
                        if (!iAmRegistered) {
                            goals.add(goal);
                            return goals;
                        }
                    } else if (goal.equals(new PredicateGoal<String>(SITUATED_PINGED, true))) {
                        if (!pingSent) {
                            goals.add(goal);
                            return goals;
                        }
                    } else if (goal.equals(new PredicateGoal<String>(COMMAND_SENT, true))) {
                        if (isSlaveAlive && !commandSent && !isFullExplored) {
                            goals.add(goal);
                            return goals;
                        }
                    }
                }
                return goals;
            }
        });
    }

    private void overridePlanSelectionStrategy() {
        this.getCapability().setPlanSelectionStrategy(new DefaultPlanSelectionStrategy() {
            @Override
            public Plan selectPlan(Goal goal, Set<Plan> capabilityPlans) {
                return super.selectPlan(goal, capabilityPlans);
            }
        });
    }

    private void enableGoalMonitoring() {
        this.addGoalListener(new GoalListener() {
            @Override
            public void goalPerformed(GoalEvent goalEvent) {
                if (goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                    // System.out.println("BDI: " + goalEvent.getGoal() + " " +
                    // "fulfilled!");
                }
            }
        });
    }

    private GoalTemplate matchesGoal(Goal goalToMatch) {
        return new GoalTemplate() {
            @Override
            public boolean match(Goal goal) {
                return goal == goalToMatch;
            }
        };
    }
}
