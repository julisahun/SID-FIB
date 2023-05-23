package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;

public class PlanBodyTest extends AbstractPlanBody {
    int counter = 0;

    @Override
    public void action() {
        System.out.println(counter++);
        GoalTest goal = (GoalTest) getGoal();
        System.out.println("Hello " + goal.getText());
        if (counter < 10)
            return;
        Belief a = getCapability().getBeliefBase().getBelief("test");
        a.setValue(false);
        setEndState(Plan.EndState.SUCCESSFUL);
    }
}
