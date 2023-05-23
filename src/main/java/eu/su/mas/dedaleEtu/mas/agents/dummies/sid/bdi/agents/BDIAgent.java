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
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.GoalTest;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.PlanBodyTest;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.SPARQLGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.CommandExplorerPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.KeepMailboxEmptyPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.RegisterPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.AgentListeningPlanBody;
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

    private HashMap<String, Goal> pingGoals = new HashMap<>();

    public BDIAgent() {
        // Create initial beliefs
        Belief<String, Boolean> iAmRegistered = new TransientPredicate<String>(I_AM_REGISTERED, false);
        Belief<String, MapaModel> ontology = new TransientBelief<String, MapaModel>(ONTOLOGY, Utils.loadOntology());
        Belief<String, HashMap<String, Node>> map = new TransientBelief<String, HashMap<String, Node>>(
                MAP, new HashMap<>());
        Belief<String, Boolean> isExplorerAlive = new TransientPredicate<String>(EXPLORER_ALIVE, false);
        Belief<String, Boolean> isCollectorAlive = new TransientPredicate<String>(COLLECTOR_ALIVE, false);
        Belief<String, Boolean> isTankerAlive = new TransientPredicate<String>(TANKER_ALIVE, false);

        Belief<String, Boolean> explorerCommanded = new TransientPredicate<String>(EXPLORER_COMMANDED, false);
        Belief<String, Boolean> collectorCommanded = new TransientPredicate<String>(COLLECTOR_COMMANDED, false);
        Belief<String, Boolean> tankerCommanded = new TransientPredicate<String>(TANKER_COMMANDED, false);

        Belief<String, Boolean> isFullExplored = new TransientPredicate<String>(IS_FULL_EXPLORED, false);

        Belief<String, HashSet> rejectedNodes = new TransientBelief<String, HashSet>(REJECTED_NODES, new HashSet<>());

        Goal pingExplorerGoal = new PingAgentGoal("explorer");
        Goal pingCollectorGoal = new PingAgentGoal("collector");
        Goal pingTankerGoal = new PingAgentGoal("tanker");

        this.pingGoals.put("explorer", pingExplorerGoal);
        this.pingGoals.put("collector", pingCollectorGoal);
        this.pingGoals.put("tanker", pingTankerGoal);

        // Add initial desires
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        Goal commandSentGoal = new PredicateGoal<String>(COMMAND_SENT, true);
        Goal situatedPingedGoal = new PredicateGoal<String>(SITUATED_PINGED, true);

        addGoal(registerGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate commandSentTemplate = matchesGoal(commandSentGoal);
        GoalTemplate situatedPingedTemplate = matchesGoal(situatedPingedGoal);

        GoalTemplate pingAgentGoalTemplate = matchesGoals(List.of(pingExplorerGoal, pingCollectorGoal, pingTankerGoal));

        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan keepMailboxEmptyPlan = new DefaultPlan(MessageTemplate.MatchAll(),
                KeepMailboxEmptyPlanBody.class);
        Plan situatedListeningPlan = new DefaultPlan(
                situatedPingedTemplate, AgentListeningPlanBody.class);
        Plan commandSentPlan = new DefaultPlan(
                commandSentTemplate, CommandExplorerPlanBody.class);

        Plan pingExplorerPlan = new DefaultPlan(
                pingAgentGoalTemplate, AgentListeningPlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(keepMailboxEmptyPlan);
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(situatedListeningPlan);
        getCapability().getPlanLibrary().addPlan(commandSentPlan);
        getCapability().getPlanLibrary().addPlan(pingExplorerPlan);

        // Init belief base

        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(map);
        getCapability().getBeliefBase().addBelief(isFullExplored);
        getCapability().getBeliefBase().addBelief(rejectedNodes);

        getCapability().getBeliefBase().addBelief(isExplorerAlive);
        getCapability().getBeliefBase().addBelief(isCollectorAlive);
        getCapability().getBeliefBase().addBelief(isTankerAlive);

        getCapability().getBeliefBase().addBelief(explorerCommanded);
        getCapability().getBeliefBase().addBelief(collectorCommanded);
        getCapability().getBeliefBase().addBelief(tankerCommanded);

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
                Boolean commandSent = (Boolean) getCapability().getBeliefBase().getBelief(EXPLORER_COMMANDED)
                        .getValue();
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
                Boolean imRegistered = (Boolean) getCapability().getBeliefBase().getBelief(I_AM_REGISTERED)
                        .getValue();
                Boolean isExplorerAlive = (Boolean) getCapability().getBeliefBase().getBelief(EXPLORER_ALIVE)
                        .getValue();
                Boolean isCollectorAlive = (Boolean) getCapability().getBeliefBase().getBelief(COLLECTOR_ALIVE)
                        .getValue();
                Boolean isTankerAlive = (Boolean) getCapability().getBeliefBase().getBelief(TANKER_ALIVE).getValue();

                if (imRegistered) {
                    if (!isExplorerAlive) {
                        System.out.println("Explorer is dead");
                        Goal ping = pingGoals.get("explorer");
                        agentGoalUpdateSet.generateGoal(ping);
                    }
                    if (!isCollectorAlive) {
                        System.out.println("Collector is dead");
                        Goal ping = pingGoals.get("collector");
                        agentGoalUpdateSet.generateGoal(ping);
                    }
                    if (!isTankerAlive) {
                        System.out.println("Tanker is dead");
                        Goal ping = pingGoals.get("tanker");
                        agentGoalUpdateSet.generateGoal(ping);
                    }
                }

                // agentGoalUpdateSet.getCurrentGoals().forEach(goal -> {
                // System.out.println("Current goal: " + goal);
                // });
                // Boolean commandSent = (Boolean)
                // getCapability().getBeliefBase().getBelief(COMMAND_SENT).getValue();
                // Boolean isSlaveAlive = (Boolean)
                // getCapability().getBeliefBase().getBelief(IS_SLAVE_ALIVE).getValue();
                // Boolean isFinished = (Boolean)
                // getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED).getValue();
                // if (isSlaveAlive && !commandSent &&
                // agentGoalUpdateSet.getCurrentGoals().size() == 0 && !isFinished) {
                // Goal commandSentGoal = new PredicateGoal<String>(COMMAND_SENT, true);
                // GoalTemplate commandSentTemplate = matchesGoal(commandSentGoal);
                // Plan commandSentPlan = new DefaultPlan(
                // commandSentTemplate, CommandExplorerPlanBody.class);
                // getCapability().getPlanLibrary().addPlan(commandSentPlan);
                // agentGoalUpdateSet.generateGoal((commandSentGoal));
                // }
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
                for (GoalDescription a : agentGoals) {
                }
                return agentGoals.stream().map(GoalDescription::getGoal).collect(java.util.stream.Collectors.toSet());
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

    private GoalTemplate matchesGoals(List<Goal> goalsToMatch) {
        return new GoalTemplate() {
            @Override
            public boolean match(Goal goal) {
                return goalsToMatch.contains(goal);
            }
        };
    }
}
