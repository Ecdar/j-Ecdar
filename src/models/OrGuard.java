package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrGuard extends Guard{

    private List<Guard> guards;

    public OrGuard(List<Guard> guards)
    {
        this.guards=guards;
    }
    public OrGuard(List<Guard>... guards)
    {
        this.guards= new ArrayList<>();
        for (List<Guard> g: guards)
            this.guards.addAll(g);
    }
    public OrGuard(Guard... guards)
    {
        this.guards= new ArrayList<>();
        for (Guard g: guards)
            this.guards.add(g);
    }

    public OrGuard(OrGuard copy, List<Clock> newClocks, List<Clock> oldClocks, List<BoolVar> newBVs, List<BoolVar> oldBVs)
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
    int getMaxConstant() {
        int max = 0;
        for (Guard g: guards)
        {
            if (g.getMaxConstant()>max)
                max = g.getMaxConstant();
        }
        return max;
    }

    @Override
    public boolean equals(Object o) { // TODO: AND(G1,G2) != AND(G2,G1) => is that okay?
        if (!(o instanceof OrGuard))
            return false;
        OrGuard other = (OrGuard) o;
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
            ret += g.toString() + " or ";
        if (guards.size()==0)
            return "";
        return ret.substring(0,ret.length()-5) + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }

    public List<Guard> getGuards() {
        return guards;
    }
}
