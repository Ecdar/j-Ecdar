package models;

import java.util.Arrays;
import java.util.Objects;

public class Edge {

    private final Location source, target;
    private final Channel chan;
    private final boolean isInput;
    private final Guard[] guards;
    private final Update[] updates;

    public Edge(Location source, Location target, Channel chan, boolean isInput, Guard[] guards, Update[] updates) {
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

    public Guard[] getGuards() {
        return guards;
    }

    public Update[] getUpdates() {
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
                Arrays.equals(guards, that.guards) &&
                Arrays.equals(updates, that.updates);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", target=" + target +
                ", chan=" + chan +
                ", isInput=" + isInput +
                ", guards=" + Arrays.toString(guards) +
                ", updates=" + Arrays.toString(updates) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, guards, updates);
    }
}
