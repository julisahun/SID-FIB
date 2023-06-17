package eu.su.mas.dedaleEtu.sid.grupo03;

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
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet.GoalDescription;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Map;
import eu.su.mas.dedaleEtu.sid.grupo03.core.MapaModel;
import eu.su.mas.dedaleEtu.sid.grupo03.core.Utils;
import eu.su.mas.dedaleEtu.sid.grupo03.core.MapaModel.NodeInfo;
import eu.su.mas.dedaleEtu.sid.grupo03.goals.CommandGoal;
import eu.su.mas.dedaleEtu.sid.grupo03.goals.PingAgentGoal;
import eu.su.mas.dedaleEtu.sid.grupo03.plans.CommandPlanBody;
import eu.su.mas.dedaleEtu.sid.grupo03.plans.KeepMailboxEmptyPlanBody;
import eu.su.mas.dedaleEtu.sid.grupo03.plans.PingAgentPlanBody;
import eu.su.mas.dedaleEtu.sid.grupo03.plans.RegisterPlanBody;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;
import javafx.util.Pair;

import org.json.JSONObject;

import static eu.su.mas.dedaleEtu.sid.grupo03.core.Constants.*;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.LinkedList;
import java.util.List;

public class BDIAgent03 extends SingleCapabilityAgent {

    private ArrayList<String> messages = new ArrayList<>();
    public String situatedName = "SituatedAgent03";
    private Goal pingAgentGoal;

    public BDIAgent03() {
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
        Belief<String, MapaModel> ontology = new TransientBelief<String, MapaModel>(ONTOLOGY, new MapaModel());
        Belief<String, Queue<Map>> mapUpdates = new TransientBelief<String, Queue<Map>>(MAP_UPDATES,
                new LinkedList<>());
        Belief<String, Boolean> isExplorerAlive = new TransientPredicate<String>(EXPLORER_ALIVE, false);
        Belief<String, Boolean> isCollectorAlive = new TransientPredicate<String>(COLLECTOR_ALIVE, false);
        Belief<String, Boolean> isTankerAlive = new TransientPredicate<String>(TANKER_ALIVE, false);

        Belief<String, Boolean> situatedPinged = new TransientPredicate<String>(SITUATED_PINGED, false);
        Belief<String, Boolean> situatedCommanded = new TransientPredicate<String>(SITUATED_COMMANDED, false);

        Belief<String, Integer> goldCapacity = new TransientBelief<String, Integer>(GOLD_CAPACITY, 0);
        Belief<String, Integer> diamondCapacity = new TransientBelief<String, Integer>(DIAMOND_CAPACITY, 0);
        Belief<String, Integer> strength = new TransientBelief<String, Integer>(STRENGTH, 0);
        Belief<String, Integer> level = new TransientBelief<String, Integer>(LEVEL, 0);

        Belief<String, Boolean> isFullExplored = new TransientPredicate<String>(IS_FULL_EXPLORED, false);

        Belief<String, HashMap<String, Integer>> rejectedNodes = new TransientBelief<String, HashMap<String, Integer>>(
                REJECTED_NODES,
                new HashMap<>());
        Belief<String, String> currentSituatedPosition = new TransientBelief<String, String>(CURRENT_SITUATED_POSITION,
                null);
        Belief<String, HashSet<Integer>> ontologyHash = new TransientBelief<String, HashSet<Integer>>(ONTOLOGY_HASHES,
                new HashSet<>());

        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(mapUpdates);
        getCapability().getBeliefBase().addBelief(isFullExplored);
        getCapability().getBeliefBase().addBelief(rejectedNodes);

        getCapability().getBeliefBase().addBelief(goldCapacity);
        getCapability().getBeliefBase().addBelief(diamondCapacity);
        getCapability().getBeliefBase().addBelief(strength);
        getCapability().getBeliefBase().addBelief(level);

        getCapability().getBeliefBase().addBelief(isExplorerAlive);
        getCapability().getBeliefBase().addBelief(isCollectorAlive);
        getCapability().getBeliefBase().addBelief(isTankerAlive);
        getCapability().getBeliefBase().addBelief(situatedPinged);
        getCapability().getBeliefBase().addBelief(situatedCommanded);
        getCapability().getBeliefBase().addBelief(currentSituatedPosition);
        getCapability().getBeliefBase().addBelief(ontologyHash);
    }

    private void initGoals() {
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        this.pingAgentGoal = new PingAgentGoal(situatedName);
        Goal commandGoal = new CommandGoal(situatedName);
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
        final BDIAgent03 that = this;
        this.getCapability().setBeliefRevisionStrategy(new DefaultBeliefRevisionStrategy() {
            @Override
            public void reviewBeliefs() {
                runMapUpdates();
                updateMapStatus();
            }

            private void updateMapStatus() {
                MapaModel map = (MapaModel) getBelief(ONTOLOGY).getValue();
                Belief isFullExplored = getBelief(IS_FULL_EXPLORED).getKey();
                Boolean wasFullExplored = map.getOpenNodes().size() == 0 && map.getClosedNodes().size() > 0
                        && !((Boolean) isFullExplored.getValue());
                if (wasFullExplored) {
                    // map.exportOntology();
                }
                isFullExplored.setValue(wasFullExplored);
            }

            private void runMapUpdates() {
                Queue<Map> mapUpdates = (Queue<Map>) getBelief(MAP_UPDATES).getValue();
                if (mapUpdates.isEmpty())
                    return;
                Map updateMap = mapUpdates.poll();
                MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
                HashMap<String, Integer> rejectedNodes = (HashMap<String, Integer>) getBelief(REJECTED_NODES)
                        .getValue();
                Utils.updateMap(updateMap, ontology, rejectedNodes);
                getBelief(REJECTED_NODES).getKey().setValue(rejectedNodes);
                JSONObject body = new JSONObject();
                body.put("ontology", ontology.getOntology());
                Utils.sendMessage(that, ACLMessage.INFORM, "map:" + body.toString(), situatedName);
            }
        });
    }

    private void overrideOptionGenerationFunction() {
        this.getCapability().setOptionGenerationFunction(new DefaultOptionGenerationFunction() {
            @Override
            public void generateGoals(GoalUpdateSet agentGoalUpdateSet) {
                final Boolean imRegistered = (Boolean) getBelief(I_AM_REGISTERED)
                        .getValue();
                final Boolean isExplorerAlive = (Boolean) getBelief(EXPLORER_ALIVE)
                        .getValue();
                final Boolean isCollectorAlive = (Boolean) getBelief(COLLECTOR_ALIVE)
                        .getValue();
                final Boolean isTankerAlive = (Boolean) getBelief(TANKER_ALIVE).getValue();

                if (imRegistered) {
                    if (!isExplorerAlive && !isCollectorAlive && !isTankerAlive && situatedName != null) {
                        Belief pingSent = getBelief(SITUATED_PINGED).getKey();
                        if (!(Boolean) pingSent.getValue()) {
                            ((PingAgentGoal) pingAgentGoal).setAgent(situatedName);
                            agentGoalUpdateSet.generateGoal(pingAgentGoal);
                            return;
                        }
                    }
                }

                if (isExplorerAlive) {
                    Boolean situatedCommanded = (Boolean) getBelief(SITUATED_COMMANDED).getValue();
                    if (!situatedCommanded) {
                        final String command = getNextExplorerCommand();
                        agentGoalUpdateSet.generateGoal(new CommandGoal(command));

                        Belief commandSent = getBelief(SITUATED_COMMANDED).getKey();
                        commandSent.setValue(true);
                        return;
                    }
                }
                if (isCollectorAlive) {
                    Boolean situatedCommanded = (Boolean) getBelief(SITUATED_COMMANDED).getValue();
                    if (!situatedCommanded) {
                        final String command = getNextCollectorCommand();
                        agentGoalUpdateSet.generateGoal(new CommandGoal(command));

                        Belief commandSent = getBelief(SITUATED_COMMANDED).getKey();
                        commandSent.setValue(true);
                        return;
                    }
                }
                if (isTankerAlive) {
                    Boolean situatedCommanded = (Boolean) getBelief(SITUATED_COMMANDED).getValue();
                    if (!situatedCommanded) {
                        final String command = getNextTankerCommand();
                        agentGoalUpdateSet.generateGoal(new CommandGoal(command));

                        Belief commandSent = getBelief(SITUATED_COMMANDED).getKey();
                        commandSent.setValue(true);
                        return;
                    }
                }
            }
        });
    }

    private String commandNotRejected(Set<String> nodes) {
        HashMap<String, Integer> rejectedNodes = (HashMap<String, Integer>) getBelief(REJECTED_NODES).getValue();

        for (final String node : nodes) {
            if (!rejectedNodes.containsKey(node)) {
                return node;
            } else if (rejectedNodes.get(node) >= WAITING_CYCLES) {
                // node rejected more than 3 times, remove from rejected nodes
                rejectedNodes.remove(node);
                return node;
            } else {
                // rejected node, increment counter
                rejectedNodes.put(node, rejectedNodes.get(node) + 1);
            }
        }
        return this.getRandomClosedNode();
    }

    private String getOpenNode() {
        MapaModel map = (MapaModel) getBelief(ONTOLOGY).getValue();
        Set<String> openNodes = map.getOpenNodes();
        return commandNotRejected(openNodes);
    }

    private String getLeastVisitedNode() {
        MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
        final String currentPosition = (String) getBelief(CURRENT_SITUATED_POSITION).getValue();
        return ontology.getLeastVisitedNode(currentPosition);
    }

    private Boolean isFullExplored() {
        MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
        return ontology.getOpenNodes().size() == 0;
    }

    private String getNextExplorerCommand() {
        if (!this.isFullExplored()) {
            final String node = this.getOpenNode();
            return node;
        }
        return this.getLeastVisitedNode();
    }

    private String getRandomNode(ArrayList<String> nodes) {
        return nodes.get(new Random().nextInt(nodes.size()));
    }

    private String getRandomOpenNode() {
        MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
        ArrayList<String> nodes = new ArrayList<String>(ontology.getOpenNodes());
        try {
            return this.getRandomNode(nodes);
        } catch (Exception e) {
            return this.getRandomClosedNode();
        }
    }

    private String getRandomClosedNode() {
        MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
        ArrayList<String> nodes = new ArrayList<String>(ontology.getClosedNodes());
        return this.getRandomNode(nodes);
    }

    private String resourceToGet() {
        Integer goldCapacity = (Integer) getBelief(GOLD_CAPACITY).getValue();
        Integer diamondCapacity = (Integer) getBelief(DIAMOND_CAPACITY).getValue();
        if (goldCapacity == 0 && diamondCapacity == 0) {
            return null;
        }
        if (goldCapacity > diamondCapacity) {
            return "gold";
        } else {
            return "diamond";
        }
    }

    private String getNextCollectorCommand() {
        final String resource = this.resourceToGet();
        final Integer strength = (Integer) getBelief(STRENGTH).getValue();
        final Integer level = (Integer) getBelief(LEVEL).getValue();

        HashMap<String, Integer> rejectedNodes = (HashMap<String, Integer>) getBelief(REJECTED_NODES).getValue();
        MapaModel ontology = (MapaModel) getCapability().getBeliefBase().getBelief(ONTOLOGY).getValue();
        final String currentPosition = (String) getBelief(CURRENT_SITUATED_POSITION).getValue();
        NodeInfo nodeInfo = ontology.getCellInfo(currentPosition);
        if (resource == null)
            return this.commandNotRejected(ontology.getOpenNodes());

        if (nodeInfo.hasResource(resource, strength, level))
            return "collect";

        HashSet<String> nodes = ontology.getResourceNodes(resource, strength, level);
        if (nodes.size() > 0)
            return this.commandNotRejected(nodes);

        if (ontology.getOpenNodes().size() != 0 && !rejectedNodes.keySet().containsAll(ontology.getOpenNodes())) {
            return this.commandNotRejected(ontology.getOpenNodes());
        }

        nodes = ontology.getResourceNodes(resource, Integer.MAX_VALUE, Integer.MAX_VALUE);
        if (nodes.size() == 0)
            return "noOp";
        // go next to a node with resource
        List<String> array = new ArrayList<String>(nodes);
        Set<String> neighborNodes = array.stream()
                .flatMap(node -> ontology.getNeighbors(node).stream())
                .filter(node -> ontology.isNonBlockingNode(node))
                .collect(Collectors.toSet());
        if (neighborNodes.contains(currentPosition)) {
            return "noOp";
        }
        return this.commandNotRejected(neighborNodes);

    }

    private String getNextTankerCommand() {
        MapaModel ontology = (MapaModel) getBelief(ONTOLOGY).getValue();
        String currentPosition = (String) getBelief(CURRENT_SITUATED_POSITION).getValue();
        if (ontology.getOpenNodes().size() == 0) {
            if (ontology.isNonBlockingNode(currentPosition)) {
                return "noOp";
            }
            return this.getRandomClosedNode();
        }
        return this.getRandomOpenNode();
    }

    private Pair<Belief, Object> getBelief(String beliefName) {
        Belief b = getCapability().getBeliefBase().getBelief(beliefName);
        return new Pair<Belief, Object>(b, b.getValue());
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
