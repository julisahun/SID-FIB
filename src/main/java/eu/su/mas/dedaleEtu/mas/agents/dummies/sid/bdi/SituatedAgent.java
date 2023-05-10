package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageMapper;
import eu.su.mas.dedaleEtu.mas.behaviours.RegisterToDF;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.BehaviourUtils.BehaviourStatus;
import jade.core.behaviours.Behaviour;
import dataStructures.tuple.Couple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SituatedAgent extends AbstractDedaleAgent {

    private MapRepresentation map;
    private HashMap<String, List<String>> messages;

    private HashMap<String, Couple<BehaviourStatus, Integer>> behavioursStatus = new HashMap<>();
    private HashMap<String, Behaviour> behaviours = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();
        List<Behaviour> lb = new ArrayList<>();
        lb.add(new RegisterToDF(this, "situated-agent", "dedale"));
        lb.add(new MessageMapper(this));
        addBehaviour(new startMyBehaviours(this, lb));
        this.messages = new HashMap<>();
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

    public void addMesssage(String header, String message) {
        if (!this.messages.containsKey(header)) {
            this.messages.put(header, new ArrayList<>());
        }
        this.messages.get(header).add(message);
    }

    public boolean getResponse(String header) {
        return this.messages.containsKey(header) && !this.messages.get(header).isEmpty();
    }

    public void registerBehaviour(String id, Behaviour b, BehaviourStatus status) {
        this.behavioursStatus.put(id, new Couple<>(status, null));
        this.behaviours.put(id, b);
    }

    public void updateStatus(String id, BehaviourStatus status) {
        this.behavioursStatus.put(id, new Couple<>(status, null));
    }

    public void updateStatus(String id, BehaviourStatus status, Integer code) {
        this.behavioursStatus.put(id, new Couple<>(status, code));
    }

    public Couple<BehaviourStatus, Integer> getStatus(String id) {
        return this.behavioursStatus.get(id);
    }

    public Behaviour getBehaviour(String id) {
        return this.behaviours.get(id);
    }
}
