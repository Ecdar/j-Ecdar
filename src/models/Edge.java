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
    private  Update[] updates;

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

    public void setUpdates(Update[] updates) {
        this.updates = updates;
    }

    public Edge(Location source, Location target, Channel chan, boolean isInput, List<List<Guard>> guards, Update[] updates) {
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
        List<Guard> temp = new ArrayList<>();
        for (List<Guard> guardList : copy.guards) {
            for (Guard g: guardList)
               temp.add(new Guard(g, clocks));
            this.guards.add(temp);
        }

        this.updates = new Update[copy.updates.length];
        for (int i = 0; i < copy.updates.length; i++) {
            this.updates[i] = new Update(copy.updates[i], clocks);
        }
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
                if (clock.equals(guard.getClock())) {
                    if (guard.getActiveBound() > constant) constant = guard.getActiveBound();
                }
            }
        }

        return constant;
    }

    // Used in determinism check to verify if two edges have exactly the same updates
    public boolean hasEqualUpdates(Edge edge){
        // If the amount of updates on edges is not the same it means they cannot have equal updates
        if(this.updates.length != edge.updates.length)
            return false;

        boolean result;

        for(int i = 0; i < this.updates.length; i++)
        {
            result = false;
            for(int j = 0; i < this.updates.length; i++)
            {
                if(this.updates[i].equals(edge.updates[j]))
                {
                    result = true;
                    break;
                }
            }
            if(!result)
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

    public void addGuards(List<List<Guard>> newGuards) {
        CDD res = new CDD(newGuards);
        this.guards = CDD.toGuards(new CDD(this.guards).conjunction(res));
    }

    public Update[] getUpdates() {
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
