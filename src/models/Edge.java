package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Edge {

    private final Location source, target;
    private final Channel chan;
    private final boolean isInput;
    private final List<Guard> guards;
    private final Update[] updates;

    public Edge(Location source, Location target, Channel chan, boolean isInput, List<Guard> guards, Update[] updates) {
        this.source = source;
        this.target = target;
        this.chan = chan;
        this.isInput = isInput;
        this.guards = guards;
        this.updates = updates;
    }

    public Edge(Edge copy, List<Clock> clocks, Location sourceR, Location targetR){
        this.source = sourceR;
        this.target = targetR;
        this.chan = copy.chan;
        this.isInput = copy.isInput;

        this.guards = new ArrayList<>();
        for (Guard g : copy.guards) {
            this.guards.add(new Guard(g, clocks));
        }

        this.updates = new Update[copy.updates.length];
        for (int i = 0; i < copy.updates.length; i++) {
            this.updates[i] = new Update(copy.updates[i], clocks);
        }
    }

    public int getMaxConstant(){
        int constant = 0;

        for(Guard guard : guards){
            if(guard.getActiveBound() > constant) constant = guard.getActiveBound();
        }

        return constant;
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

    public void addGuards(List<Guard> newGuards) {
        this.guards.addAll(newGuards);
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
                Arrays.equals(guards.toArray(), that.guards.toArray()) &&
                Arrays.equals(updates, that.updates);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source +
                ", target=" + target +
                ", chan=" + chan +
                ", isInput=" + isInput +
                ", guards=" + Arrays.toString(guards.toArray()) +
                ", updates=" + Arrays.toString(updates) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, guards, updates);
    }
}
