package models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Edge {

    private Location source, target;
    private Channel chan;
    private boolean isInput;
    private BooleanExpression booleanExpression;
    private List<Update> updates;

    public void setSource(Location source) {
        this.source = source;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    public Channel getChan() {
        return chan;
    }

    public void setChan(Channel chan) {
        this.chan = chan;
    }

    public void setInput(boolean input) {
        isInput = input;
    }

    public void setGuard(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    public Edge(Location source, Location target, Channel chan, boolean isInput, BooleanExpression guards, List<Update> updates) {
        this.source = source;
        this.target = target;
        this.chan = chan;
        this.isInput = isInput;
        this.booleanExpression = guards;
        this.updates = updates;
    }

    public Edge(Edge copy, List<Clock> newClocks, List<BoolVar> newBVs, Location sourceR, Location targetR, List<Clock> oldClocks, List<BoolVar> oldBVs) {
        this(
            sourceR,
            targetR,
            copy.chan,
            copy.isInput,
            copy.booleanExpression.copy(newClocks, oldClocks, newBVs, oldBVs),
            copy.updates
                .stream()
                .map(update -> update.copy(
                        newClocks, oldClocks, newBVs, oldBVs
                ))
                .collect(Collectors.toList())
        );
    }

    public Edge(Edge copy, List<Clock> newClocks, List<BoolVar> newBVs, List<Clock> oldClocks, List<BoolVar> oldBVs) {
        this(copy, newClocks, newBVs, copy.getSource(), copy.getTarget(), oldClocks, oldBVs);
    }

    public CDD getGuardCDD() {
        return new CDD(booleanExpression);
    }

    public int getMaxConstant(Clock clock) {
        return booleanExpression.getMaxConstant(clock);
    }

    // Used in determinism check to verify if two edges have exactly the same updates
    public boolean hasEqualUpdates(Edge edge) {
        return Arrays.equals(
            getUpdates().toArray(), edge.getUpdates().toArray()
        );
    }

    public Location getSource() {
        return source;
    }

    public Location getTarget() {
        return target;
    }

    public Channel getChannel() {
        return chan;
    }

    public boolean isInput() {
        return isInput;
    }

    public BooleanExpression getGuard() {
        return booleanExpression;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge)) return false;

        Edge edge = (Edge) obj;
        if (source == null && edge.source != null) {
            return false;
        }
        if (target == null && edge.target != null) {
            return false;
        }
        if (chan == null && edge.chan != null) {
            return false;
        }
        if (booleanExpression == null && edge.booleanExpression != null) {
            return false;
        }
        return isInput == edge.isInput &&
                source != null && source.equals(edge.source) &&
                target != null && target.equals(edge.target) &&
                chan != null && chan.equals(edge.chan) &&
                booleanExpression != null && booleanExpression.equals(edge.booleanExpression) &&
                hasEqualUpdates(edge);
    }

    @Override
    public String toString() {
        return "(" +
                source + " - " +
                chan.getName() +
                (isInput ? "?" : "!") + " - " +
                booleanExpression + " - " +
                Arrays.toString(this.updates.toArray()) + " - " +
                target + ")\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, booleanExpression, updates);
    }
}
