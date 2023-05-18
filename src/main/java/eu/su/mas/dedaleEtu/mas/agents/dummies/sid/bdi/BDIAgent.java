package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

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
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.lang.acl.MessageTemplate;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.net.URL;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

public class BDIAgent extends SingleCapabilityAgent {

    private final String FILE_NAME = "ontology";

    public BDIAgent() {
        // Create initial beliefs
        Belief<String, Boolean> iAmRegistered = new TransientPredicate<String>(I_AM_REGISTERED, false);
        Belief<String, OntModel> ontology = new TransientBelief<String, OntModel>(ONTOLOGY, Utils.loadOntology());
        Belief<String, Boolean> isSlaveAlive = new TransientPredicate<String>(IS_SLAVE_ALIVE, false);
        Belief<String, HashMap<String, List<String>>> map = new TransientBelief<String, HashMap<String, List<String>>>(
                MAP, new HashMap<>());
        Belief<String, Boolean> isFullExplored = new TransientPredicate<String>(IS_FULL_EXPLORED, false);

        // Add initial desires
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        Goal findSituatedGoal = new SPARQLGoal<String>(ONTOLOGY, QUERY_SITUATED_AGENT);
        Goal situatedListeningGoal = new PredicateGoal<String>(IS_SLAVE_ALIVE, true);
        Goal exploreMapGoal = new PredicateGoal<String>(IS_FULL_EXPLORED, true);

        addGoal(registerGoal);
        addGoal(findSituatedGoal);
        addGoal(situatedListeningGoal);
        addGoal(exploreMapGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate findSituatedTemplate = matchesGoal(findSituatedGoal);
        GoalTemplate situatedListeningTemplate = matchesGoal(situatedListeningGoal);
        GoalTemplate exploreMapTemplate = matchesGoal(exploreMapGoal);
        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan findSituatedPlan = new DefaultPlan(
                findSituatedTemplate, FindSituatedPlanBody.class);
        // Plan keepMailboxEmptyPlan = new DefaultPlan(MessageTemplate.MatchAll(),
        // KeepMailboxEmptyPlanBody.class);
        Plan situatedListeningPlan = new DefaultPlan(
                situatedListeningTemplate, SituatedListeningPlanBody.class);
        Plan exploreMapPlan = new DefaultPlan(
                exploreMapTemplate, ExploreMapPlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(situatedListeningPlan);
        getCapability().getPlanLibrary().addPlan(findSituatedPlan);
        getCapability().getPlanLibrary().addPlan(exploreMapPlan);
        // getCapability().getPlanLibrary().addPlan(keepMailboxEmptyPlan);

        // Init belief base
        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(isSlaveAlive);
        getCapability().getBeliefBase().addBelief(map);
        getCapability().getBeliefBase().addBelief(isFullExplored);

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
                // A GoalUpdateSet contains the goal status for the agent:
                // - Current goals (.getCurrentGoals)
                // - Generated goals, existing but not adopted yet (.getGeneratedGoals)
                // - Dropped goals, discarded forever (.getDroppedGoals)
                // This method should update these three sets (current,
                // generated, dropped).
                // The default implementation does nothing
            }
        });
    }

    private void overrideDeliberationFunction() {
        this.getCapability().setDeliberationFunction(new DefaultDeliberationFunction() {
            @Override
            public Set<Goal> filter(Set<GoalUpdateSet.GoalDescription> agentGoals) {
                Boolean iAmRegistered = (Boolean) getCapability().getBeliefBase().getBelief(I_AM_REGISTERED).getValue();
                Boolean isSlaveAlive = (Boolean) getCapability().getBeliefBase().getBelief(IS_SLAVE_ALIVE).getValue();
                for (GoalUpdateSet.GoalDescription goalDescription : agentGoals) {
                    System.out.println("Goal description: " + goalDescription);
                    Goal goal = goalDescription.getGoal();
                    if (goal.equals(new PredicateGoal<String>(I_AM_REGISTERED, true))) {
                        if (!iAmRegistered)
                            return Set.of(goal);
                    } else if (goal.equals(new PredicateGoal<String>(IS_SLAVE_ALIVE, true))) {
                        if (!isSlaveAlive)
                            return Set.of(goal);
                    } else if (goal.equals(new PredicateGoal<String>(IS_FULL_EXPLORED, true))) {
                        if (isSlaveAlive)
                            return Set.of(goal);
                    }
                }
                return super.filter(agentGoals);
            }
        });
    }

    private void overridePlanSelectionStrategy() {
        this.getCapability().setPlanSelectionStrategy(new DefaultPlanSelectionStrategy() {
            @Override
            public Plan selectPlan(Goal goal, Set<Plan> capabilityPlans) {
                // This method should return a plan from a list of
                // valid (ordered) plans for fulfilling a particular goal.
                // The default implementation just chooses
                // the first plan of the list.
                System.out.println("Plan selection strategy");
                return super.selectPlan(goal, capabilityPlans);
            }
        });
    }

    private void enableGoalMonitoring() {
        this.addGoalListener(new GoalListener() {
            @Override
            public void goalPerformed(GoalEvent goalEvent) {
                if (goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                    System.out.println("BDI: " + goalEvent.getGoal() + " " +
                            "fulfilled!");
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
