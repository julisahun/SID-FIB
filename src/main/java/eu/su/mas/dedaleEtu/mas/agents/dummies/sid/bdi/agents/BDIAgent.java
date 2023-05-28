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
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.CommandGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.CommandPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.KeepMailboxEmptyPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.PingAgentPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans.RegisterPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import eu.su.mas.dedaleEtu.mas.knowledge.Node;
import eu.su.mas.dedaleEtu.mas.knowledge.Map;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.NodeType;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashSet;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

public class BDIAgent extends SingleCapabilityAgent {

    private ArrayList<String> messages = new ArrayList<>();
    public final static String situatedName = "situated";
    private Goal pingAgentGoal;

    public BDIAgent() {

        initBeliefs();

        initGoals();

        // Add a goal listener to track events
        enableGoalMonitoring();

        // Override BDI cycle meta-functions, if needed
        overrideBeliefRevisionStrategy();
        overrideOptionGenerationFunction();
        overrideDeliberationFunction();
        overridePlanSelectionStrategy();
    }

    private void initBeliefs() {
        Belief<String, Boolean> iAmRegistered = new TransientPredicate<String>(I_AM_REGISTERED, false);
        Belief<String, MapaModel> ontology = new TransientBelief<String, MapaModel>(ONTOLOGY, Utils.loadOntology());
        Belief<String, Map> map = new TransientBelief<String, Map>(MAP, new Map());
        Belief<String, Queue<Map>> mapUpdates = new TransientBelief<String, Queue<Map>>(MAP_UPDATES,
                new LinkedList<>());
        Belief<String, Boolean> isExplorerAlive = new TransientPredicate<String>(EXPLORER_ALIVE, false);
        Belief<String, Boolean> isCollectorAlive = new TransientPredicate<String>(COLLECTOR_ALIVE, false);
        Belief<String, Boolean> isTankerAlive = new TransientPredicate<String>(TANKER_ALIVE, false);

        Belief<String, Boolean> situatedPinged = new TransientPredicate<String>(SITUATED_PINGED, false);
        Belief<String, Boolean> situatedCommanded = new TransientPredicate<String>(SITUATED_COMMANDED, false);

        Belief<String, Boolean> isFullExplored = new TransientPredicate<String>(IS_FULL_EXPLORED, false);

        Belief<String, HashSet<String>> rejectedNodes = new TransientBelief<String, HashSet<String>>(REJECTED_NODES,
                new HashSet<>());
        Belief<String, String> currentSituatedPosition = new TransientBelief<String, String>(CURRENT_SITUATED_POSITION,
                null);
        Belief<String, Boolean> mapOutOfSync = new TransientPredicate<String>(MAP_OUT_OF_SYNC, false);

        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(map);
        getCapability().getBeliefBase().addBelief(mapUpdates);
        getCapability().getBeliefBase().addBelief(isFullExplored);
        getCapability().getBeliefBase().addBelief(rejectedNodes);

        getCapability().getBeliefBase().addBelief(isExplorerAlive);
        getCapability().getBeliefBase().addBelief(isCollectorAlive);
        getCapability().getBeliefBase().addBelief(isTankerAlive);
        getCapability().getBeliefBase().addBelief(situatedPinged);
        getCapability().getBeliefBase().addBelief(situatedCommanded);
        getCapability().getBeliefBase().addBelief(currentSituatedPosition);
        getCapability().getBeliefBase().addBelief(mapOutOfSync);
    }

    private void initGoals() {
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        this.pingAgentGoal = new PingAgentGoal(BDIAgent.situatedName);
        Goal commandGoal = new CommandGoal(BDIAgent.situatedName);
        addGoal(registerGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate commandSentTemplate = matchesGoal(commandGoal);
        GoalTemplate pingAgentGoalTemplate = matchesGoal(this.pingAgentGoal);

        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan keepMailboxEmptyPlan = new DefaultPlan(MessageTemplate.MatchAll(),
                KeepMailboxEmptyPlanBody.class);
        Plan commandSentPlan = new DefaultPlan(
                commandSentTemplate, CommandPlanBody.class);
        Plan pingExplorerPlan = new DefaultPlan(
                pingAgentGoalTemplate, PingAgentPlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(keepMailboxEmptyPlan);
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(commandSentPlan);
        getCapability().getPlanLibrary().addPlan(pingExplorerPlan);
    }

    private void overrideBeliefRevisionStrategy() {
        this.getCapability().setBeliefRevisionStrategy(new DefaultBeliefRevisionStrategy() {
            @Override
            public void reviewBeliefs() {
                runMapUpdates();
                updateMapStatus();
            }

            private void updateMapStatus() {
                MapaModel map = (MapaModel) getCapability().getBeliefBase().getBelief(ONTOLOGY).getValue();
                Set<String> openNodes = map.getOpenNodes();
                Belief<String, Boolean> isFullExplored = (Belief<String, Boolean>) getCapability().getBeliefBase()
                        .getBelief(IS_FULL_EXPLORED);
                if (map.getOpenNodes().size() == 0 && map.getClosedNodes().size() > 0 && !isFullExplored.getValue()) {
                    Utils.saveOntology(map.model);
                }
                isFullExplored.setValue(openNodes.size() == 0);
            }

            private void runMapUpdates() {
                Belief mapOutOfSync = getCapability().getBeliefBase().getBelief(MAP_OUT_OF_SYNC);
                if ((Boolean) mapOutOfSync.getValue()) {
                    syncMap();
                    mapOutOfSync.setValue(false);
                }
                Queue<Map> mapUpdates = (Queue<Map>) getCapability().getBeliefBase().getBelief(MAP_UPDATES).getValue();
                if (mapUpdates.isEmpty())
                    return;
                Map updateMap = mapUpdates.poll();
                MapaModel ontology = (MapaModel) getCapability().getBeliefBase().getBelief(ONTOLOGY).getValue();
                Belief mapBelief = getCapability().getBeliefBase().getBelief(MAP);
                Map currentMap = (Map) mapBelief.getValue();
                currentMap.update(updateMap, ontology);
                mapBelief.setValue(currentMap);
            }

            private void syncMap() {
                MapaModel ontology = (MapaModel) getCapability().getBeliefBase().getBelief(ONTOLOGY).getValue();
                Belief mapBelief = getCapability().getBeliefBase().getBelief(MAP);
                Map currentMap = (Map) mapBelief.getValue();
                currentMap.syncWithOntology(ontology);
                mapBelief.setValue(currentMap);
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
                    if (!isExplorerAlive && !isCollectorAlive && !isTankerAlive) {
                        Belief pingSent = getCapability().getBeliefBase().getBelief(SITUATED_PINGED);
                        if (!(Boolean) pingSent.getValue()) {
                            agentGoalUpdateSet.generateGoal(pingAgentGoal);
                            return;
                        }
                    }
                }

                if (isExplorerAlive) {
                    Boolean situatedCommanded = (Boolean) getCapability().getBeliefBase()
                            .getBelief(SITUATED_COMMANDED).getValue();
                    Boolean isFullExplored = (Boolean) getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED)
                            .getValue();
                    if (!situatedCommanded && !isFullExplored) {
                        agentGoalUpdateSet.generateGoal(new CommandGoal(getNextExplorerCommand()));
                        Belief commandSent = getCapability().getBeliefBase().getBelief(SITUATED_COMMANDED);
                        commandSent.setValue(true);
                        return;
                    }
                }
            }
        });
    }

    private String getOpenNode() {
        Map map = (Map) getCapability().getBeliefBase().getBelief(MAP).getValue();
        return map.getOpenNode();
    }

    private String getLeastVisitedNode() {
        HashMap<String, Node> map = (HashMap<String, Node>) getCapability().getBeliefBase().getBelief(MAP).getValue();
        HashSet<String> rejectedNodes = (HashSet<String>) getCapability().getBeliefBase().getBelief(REJECTED_NODES)
                .getValue();
        String currentPos = (String) getCapability().getBeliefBase().getBelief(CURRENT_SITUATED_POSITION).getValue();
        Node currentNode = map.get(currentPos);
        int min = Integer.MAX_VALUE;
        String minNode = null;
        for (String neighbor : currentNode.getNeighbors()) {
            if (rejectedNodes.contains(neighbor))
                continue;
            Node node = map.get(neighbor);
            if (node.getTimesVisited() < min) {
                min = node.getTimesVisited();
                minNode = neighbor;
            }
        }

    }

    private String getNextExplorerCommand() {
        Boolean isFullExplored = (Boolean) getCapability().getBeliefBase().getBelief(IS_FULL_EXPLORED).getValue();
        if (!isFullExplored) {
            return getOpenNode();
        }
        return "";
        // return getLeastVisitedNode();
    }

    // private String getNextCollectorCommand() {

    // }

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
                    java.util.Map<Capability, Set<GoalDescription>> capabilityGoals) {
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
                return goal.getClass().equals(goalToMatch.getClass());
                // return goal == goalToMatch;
            }
        };
    }

}
