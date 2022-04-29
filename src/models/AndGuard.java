package models;

import java.util.*;

public class AndGuard extends Guard{

    private List<Guard> guards;

    public AndGuard(List<Guard> guards)
    {
        this.guards=guards;
        List<Integer> indices = new ArrayList<>();
        for (int i=0; i<this.guards.size(); i++)
            if (this.guards.get(i) instanceof TrueGuard)
                indices.add(i);
        Collections.reverse(indices);
        for (int i : indices)
            this.guards.remove(i);
        if (this.guards.isEmpty())
            this.guards.add(new TrueGuard());
    }
    public AndGuard(List<Guard>... guards)
    {
        this.guards= new ArrayList<>();
        for (List<Guard> g: guards)
            this.guards.addAll(g);


        List<Integer> indices = new ArrayList<>();
        for (int i=0; i<this.guards.size(); i++)
            if (this.guards.get(i) instanceof TrueGuard)
                indices.add(i);
        Collections.reverse(indices);
        for (int i : indices)
            this.guards.remove(i);
        if (this.guards.isEmpty())
            this.guards.add(new TrueGuard());
    }
    public AndGuard(Guard... guards)
    {
        this.guards= new ArrayList<>();
        for (Guard g : guards)
           this.guards.add(g);

        List<Integer> indices = new ArrayList<>();
        for (int i=0; i<this.guards.size(); i++)
            if (this.guards.get(i) instanceof TrueGuard)
                indices.add(i);
        Collections.reverse(indices);
        for (int i : indices)
            this.guards.remove(i);
        if (this.guards.isEmpty())
            this.guards.add(new TrueGuard());

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
            if (g instanceof TrueGuard)
                this.guards.add(new TrueGuard());
            if (g instanceof AndGuard)
                this.guards.add(new AndGuard( (AndGuard) g, newClocks, oldClocks, newBVs, oldBVs));
            if (g instanceof OrGuard)
                this.guards.add(new OrGuard( (OrGuard) g, newClocks, oldClocks, newBVs, oldBVs));
        }
        List<Integer> indices = new ArrayList<>();
        for (int i=0; i<guards.size(); i++)
            if (guards.get(i) instanceof TrueGuard)
                indices.add(i);
        Collections.reverse(indices);
        for (int i : indices)
            guards.remove(i);
        if (this.guards.isEmpty())
            this.guards.add(new TrueGuard());

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
        if (!(o instanceof AndGuard))
            return false;
        AndGuard other = (AndGuard) o;
        if (other.guards.size()!=guards.size())
            return  false;
        if (guards.size()==0)
            return true;
        for (int i =0; i< other.guards.size(); i++)
            if (!guards.get(i).equals(other.guards.get(i)))
                return false;
        return true;
    }

    @Override
    public String toString() {




        String ret = "(";
        for (Guard g: guards)
            ret += g.toString() + " && ";
        if (guards.size()==0)
            return "";
        if (guards.size()==1)
            return guards.get(0).toString();
        return ret.substring(0,ret.length()-4) + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(false);
    }

    public List<Guard> getGuards() {
        return guards;
    }
}
