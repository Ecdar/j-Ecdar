package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Edge {

    private  Location source, target;
    private  Channel chan;
    private  boolean isInput;
    private  List<List<Guard>> guards;
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

    public void setGuards(List<List<Guard>> guards) {
        this.guards = guards;
    }


    public Edge(Location source, Location target, Channel chan, boolean isInput, List<List<Guard>> guards, List<Update> updates) {
        this.source = source;
        this.target = target;
        this.chan = chan;
        this.isInput = isInput;
        this.guards = guards;
        this.updates = updates;
    }

    public Edge(Edge copy, List<Clock> clocks,  List<BoolVar> BVs, Location sourceR, Location targetR, List<Clock> oldClocks){
        this.source = sourceR;
        this.target = targetR;
        this.chan = copy.chan;
        this.isInput = copy.isInput;

        this.guards = new ArrayList<>();
        List<Guard> temp = new ArrayList<>();
        for (List<Guard> guardList : copy.guards) {
            for (Guard g: guardList) {
                if (g instanceof ClockGuard)
                    temp.add(new ClockGuard((ClockGuard) g,  clocks,oldClocks));
                if (g instanceof BoolGuard)
                    temp.add(new BoolGuard((BoolGuard) g));
                if (g instanceof FalseGuard)
                    temp.add(new FalseGuard());
            }
            this.guards.add(temp);
        }

        List<Update> updates = new ArrayList<>();
        for (Update update: copy.getUpdates())
        {
            if (update instanceof BoolUpdate)
            {
                updates.add(new BoolUpdate((BoolUpdate) update, BVs));
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

        for(List<Guard> guardList : guards) {
            for (Guard guard : guardList) {
                if (guard instanceof ClockGuard && !((ClockGuard) guard).isDiagonal() && clock.equals( ((ClockGuard) guard).getClock_i())) {
                    if (((ClockGuard)guard).getBound() > constant) constant = ((ClockGuard) guard).getBound();
                }
            }
        }

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

    public List<List<Guard>> getGuards() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge that = (Edge) o;
        /*System.out.println(toString());
        System.out.println(that.toString());
        System.out.println(isInput == that.isInput);
        System.out.println(source.equals(that.source));
        System.out.println(target.equals(that.target));
        System.out.println(chan.equals(that.chan));
        System.out.println(Arrays.equals(Arrays.stream(guards.toArray()).toArray(), Arrays.stream(that.guards.toArray()).toArray()));
        System.out.println(Arrays.equals(updates, that.updates));*/
        return isInput == that.isInput &&
                source.equals(that.source) &&
                target.equals(that.target) &&
                chan.equals(that.chan) &&
                // TODO: did the stream thing work?
                //that.getGuardCDD().equiv(getGuardCDD()) &&
                Arrays.equals(Arrays.stream(guards.toArray()).toArray(), Arrays.stream(that.guards.toArray()).toArray()) &&
                Arrays.equals(Arrays.stream(updates.toArray()).toArray(), Arrays.stream(that.updates.toArray()).toArray());
    }

    @Override
    public String toString() {
        return "(" +
                source + " - " +
                chan.getName() +
                (isInput? "?":"!")  +" - " +
                guards +" - " +
                Arrays.toString(this.guards.toArray()) +" - " +
                Arrays.toString(this.updates.toArray()) +" - " +
                target  + ")\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, chan, isInput, guards, updates);
    }
}
