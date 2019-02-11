package logic;

import models.Guard;
import models.Edge;

import java.util.ArrayList;
import java.util.List;

public class Transition {
    private State source, target;
    private List<Edge> edges;

    public Transition(State source, State target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public List<Guard> getGuards() {
        List<Guard> guards = new ArrayList<>();
        for (Edge t : edges) {
            if (t != null) guards.addAll(t.getGuards());
        }
        return guards;
    }
}