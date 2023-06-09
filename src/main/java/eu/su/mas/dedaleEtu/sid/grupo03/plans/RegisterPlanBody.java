package eu.su.mas.dedaleEtu.sid.grupo03.plans;

import static eu.su.mas.dedaleEtu.sid.grupo03.core.Constants.*;

import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class RegisterPlanBody extends BeliefGoalPlanBody {
    @Override
    public void execute() {
        Agent agent = this.myAgent;
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("master");
        sd.setType("bdi");
        sd.addOntologies("polydama-mapstate");
        try {
            DFService.register(this.myAgent, dfd);
            getBeliefBase().updateBelief(I_AM_REGISTERED, true);
            // This is valid but redundant
            // (because the goal implementation will check the belief anyway):
            setEndState(Plan.EndState.SUCCESSFUL);
        } catch (FIPAException e) {
            setEndState(Plan.EndState.FAILED);
            e.printStackTrace();
        }
    }
}
