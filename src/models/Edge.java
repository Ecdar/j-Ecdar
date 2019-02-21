package models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Edge {

    private final Location source, target;
    private final Channel chan;
    private final boolean isInput;
    private final List<Guard> guards;
    private final List<Update> updates;

    public Edge(Location source, Location target, Channel chan, boolean isInput, List<Guard> guards, List<Update> updates) {
        this.source = source;
        this.target = target;
        this.chan = chan;
        this.isInput = isInput;
        this.guards = guards;
        this.updates = updates;
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

    public List<Guard> getGuards() {
        return guards;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge that = (Edge) o;
        return isInput == that.isInput &&
                source.equals(that.source) &&
                target.equals(that.target) &&
                chan.equals(that.chan) &&
                Arrays.equals(guards.toArray(), that.guards.toArray()) &&
                Arrays.equals(updates.toArray(), that.updates.toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, guards, updates);
    }
}
