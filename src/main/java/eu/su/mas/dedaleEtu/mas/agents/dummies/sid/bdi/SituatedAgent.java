package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageMapper;
import eu.su.mas.dedaleEtu.mas.behaviours.RegisterToDF;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SituatedAgent extends AbstractDedaleAgent {

    private MapRepresentation map;

    @Override
    protected void setup() {
        super.setup();
        List<Behaviour> lb = new ArrayList<>();
        lb.add(new RegisterToDF(this, "situated-agent", "dedale"));
        lb.add(new MessageMapper(this));
        addBehaviour(new startMyBehaviours(this, lb));
    }

    public void setMap(MapRepresentation newMap) {
        if (this.map == null)
            this.map = newMap;
        else
            this.map.mergeMap(newMap.getSerializableGraph());
    }

    public MapRepresentation getMap() {
        return this.map;
    }
}
