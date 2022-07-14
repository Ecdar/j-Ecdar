package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Move {

    private final SymbolicLocation source;
    private SymbolicLocation target;
    private final List<Edge> edges;
    private CDD guardCDD;
    private List<Update> updates;

    public Move(SymbolicLocation source, SymbolicLocation target, List<Edge> edges) {

        this.source = source;
        this.target = target;
        this.edges = edges;
        guardCDD = CDD.cddTrue();
        this.updates = new ArrayList<>();
        for (Edge e : edges) {
            CDD guardCDD1 = e.getGuardCDD();
            guardCDD = guardCDD.conjunction(guardCDD1);
            updates.addAll(e.getUpdates());
        }

    }

    /**
     * Return the enabled part of a move based on guard, source invariant and predated target invariant
     **/
    public CDD getEnabledPart() {
        CDD sourceInvariant = getSource().getInvariantCDD();
        CDD targetInvariant = getTarget().getInvariantCDD();
        return getGuardCDD().conjunction(targetInvariant.transitionBack(this)).conjunction(sourceInvariant);
    }

    public void conjunctCDD(CDD cdd)
    {
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

    public Guard getGuards(List <Clock> relevantClocks) {
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

    public void setTarget(SymbolicLocation loc) {
        target = loc;
    }
}
