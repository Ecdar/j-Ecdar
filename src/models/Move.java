package models;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private final SymbolicLocation source, target;
    private final List<Edge> edges;
    private CDD guardCDD;
    private List<Update> updates;

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {
        this.source = source;
        this.target = target;
        this.edges = edges;
        this.updates = new ArrayList<>();
        guardCDD = CDD.cddTrue();
        for (Edge edge : edges) {
            guardCDD = guardCDD.conjunction(edge.getGuardCDD());
            updates.addAll(edge.getUpdates());
        }
    }

    /**
     * Return the enabled part of a move based on guard, source invariant and predated target invariant
     **/
    public CDD getEnabledPart() {
        CDD targetInvariant = getTarget().getInvariantCDD();
        CDD sourceInvariant = getSource().getInvariantCDD();
        return getGuardCDD()
                .conjunction(targetInvariant.transitionBack(this))
                .conjunction(sourceInvariant);
    }

    public void conjunctCDD(CDD cdd) {
        guardCDD = guardCDD.conjunction(cdd);
    }

    public SymbolicLocation getSource() {
        return source;
    }

    public SymbolicLocation getTarget() {
        return target;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public CDD getGuardCDD() {
        return (guardCDD);
    }

    public Guard getGuards(List<Clock> relevantClocks) {
        return CDD.toGuardList(guardCDD, relevantClocks);
    }

    public void setGuards(CDD guardCDD) {
        this.guardCDD = guardCDD;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Update> updates) {
        this.updates = updates;
    }
}
