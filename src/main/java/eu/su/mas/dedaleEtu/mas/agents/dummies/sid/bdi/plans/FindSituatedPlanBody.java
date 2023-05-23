package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.plans;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.Utils;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import jade.domain.FIPAException;

public class FindSituatedPlanBody extends BeliefGoalPlanBody {
    @Override
    protected void execute() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("dedale");
        template.addServices(templateSd);
        DFAgentDescription[] results;
        try {
            results = DFService.search(this.myAgent, template);
            for (DFAgentDescription agent : results) {
                AID provider = agent.getName();
                if (!provider.getName().contains("slave"))
                    continue;
                updateOntology(provider.getLocalName());
                setEndState(Plan.EndState.SUCCESSFUL);
            }
            // if results.length == 0, no endState is set,
            // so the plan body will run again (if the goal still holds)
        } catch (FIPAException e) {
            setEndState(Plan.EndState.FAILED);
            e.printStackTrace();
        }
    }

    private void updateOntology(String situatedAgentName) {
        // Utils.addIndividual(myAgent, "Agent", situatedAgentName);
    }
}
