package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AndGuard extends Guard{

    private List<Guard> guards;

    public AndGuard(List<Guard> guards)
    {
        this.guards=guards;
    }

    public AndGuard(AndGuard copy, List<Clock> newClocks,List<Clock> oldClocks,   List<BoolVar> newBVs, List<BoolVar> oldBVs)
    {
        this.guards=new ArrayList<>();
        for (Guard g : copy.guards)
        {
            if (g instanceof ClockGuard)
                this.guards.add(new ClockGuard((ClockGuard) g,  newClocks,oldClocks));
            if (g instanceof BoolGuard)
                this.guards.add(new BoolGuard((BoolGuard) g, newBVs, oldBVs));
            if (g instanceof FalseGuard)
                this.guards.add(new FalseGuard());
            if (g instanceof AndGuard)
                this.guards.add(new AndGuard( (AndGuard) g, newClocks, oldClocks, newBVs, oldBVs));
            if (g instanceof OrGuard)
                this.guards.add(new OrGuard( (OrGuard) g, newClocks, oldClocks, newBVs, oldBVs));
        }
    }


    @Override
    public boolean equals(Object o) { // TODO: AND(G1,G2) != AND(G2,G1) => is that okay?
        if (!(o instanceof AndGuard))
            return false;
        AndGuard other = (AndGuard) o;
        if (other.guards.size()!=guards.size())
            return  false;
        for (int i =0; i<= other.guards.size(); i++)
            if (!guards.get(i).equals(other.guards.get(i)))
                return false;
        return true;
    }

    @Override
    public String toString() {
        String ret = "(";
        for (Guard g: guards)
            ret += g.toString() + " and ";
        if (guards.size()==0)
            return "";
        return ret.substring(0,ret.length()-5) + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }
}
