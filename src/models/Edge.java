package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Edge {

    private  Location source, target;
    private  Channel chan;
    private  boolean isInput;
    private  Guard guards;
    private  List<Update> updates;

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

    public void setGuards(Guard guards) {
        this.guards = guards;
    }


    public Edge(Location source, Location target, Channel chan, boolean isInput, Guard guards, List<Update> updates) {
        this.source = source;
        this.target = target;
        this.chan = chan;
        this.isInput = isInput;
        this.guards = guards;
        this.updates = updates;
    }

    public Edge(Edge copy, List<Clock> clocks,  List<BoolVar> BVs, Location sourceR, Location targetR, List<Clock> oldClocks, List<BoolVar> oldBVs){
        this.source = sourceR;
        this.target = targetR;
        this.chan = copy.chan;
        this.isInput = copy.isInput;

        if (copy.guards instanceof ClockGuard)
            this.guards =new ClockGuard((ClockGuard) copy.guards,  clocks,oldClocks);
        if (copy.guards instanceof BoolGuard)
            this.guards =new BoolGuard((BoolGuard) copy.guards, BVs, oldBVs);
        if (copy.guards instanceof FalseGuard)
            this.guards =new FalseGuard();
        if (copy.guards instanceof TrueGuard)
            this.guards =new TrueGuard();
        if (copy.guards instanceof AndGuard)
            this.guards =new AndGuard( (AndGuard) copy.guards, clocks, oldClocks, BVs, oldBVs);
        if (copy.guards instanceof OrGuard)
            this.guards =new OrGuard( (OrGuard) copy.guards, clocks, oldClocks, BVs, oldBVs);

        List<Update> updates = new ArrayList<>();
        for (Update update: copy.getUpdates())
        {
            if (update instanceof BoolUpdate)
            {
                updates.add(new BoolUpdate((BoolUpdate) update, BVs, oldBVs));
            }
            else
            {
                updates.add(new ClockUpdate((ClockUpdate) update, clocks, oldClocks));

            }

        }
        this.updates= updates;
    }



    public CDD getGuardCDD()
    {
        CDD res = new CDD(guards);
        return res;
    }





    public int getMaxConstant(Clock clock){
        int constant = 0;
        constant = guards.getMaxConstant();
        return constant;
    }

    // Used in determinism check to verify if two edges have exactly the same updates
    public boolean hasEqualUpdates(Edge edge){
        // If the amount of updates on edges is not the same it means they cannot have equal updates
        for (Update thisU : getUpdates()) {
            boolean matched = false;
            for (Update thatU : edge.getUpdates())
                if (thisU.equals(thatU))
                    matched=true;
            if (!matched)
                return false;
        }

        for (Update thatU : edge.getUpdates()) {
            boolean matched = false;
            for (Update thisU : getUpdates())
                if (thisU.equals(thatU))
                    matched=true;
            if (!matched)
                return false;
        }

        return true;
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

    public Guard getGuards() {
        return guards;
    }

   /* public void addGuards(List<List<Guard>> newGuards) {
        CDD res = new CDD(newGuards);
        this.guards = CDD.toGuards(new CDD(this.guards).conjunction(res),clocks);
    }*/

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
        if (guards == null && edge.guards != null) {
            return false;
        }
        return isInput == edge.isInput &&
                source != null && source.equals(edge.source) &&
                target != null && target.equals(edge.target) &&
                chan != null && chan.equals(edge.chan) &&
                guards != null && guards.equals(edge.guards) &&
                hasEqualUpdates(edge);
    }

    @Override
    public String toString() {
        return "(" +
                source + " - " +
                chan.getName() +
                (isInput? "?":"!")  +" - " +
                guards +" - " +
                Arrays.toString(this.updates.toArray()) +" - " +
                target  + ")\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, guards, updates);
    }
}
