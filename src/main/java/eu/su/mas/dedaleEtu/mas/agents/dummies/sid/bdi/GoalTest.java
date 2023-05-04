package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.goal.Goal;

public class GoalTest implements Goal {
    private final String text;
    public GoalTest(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
}
