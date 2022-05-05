package models;

import java.security.URIParameter;
import java.util.*;
import java.util.stream.Collectors;

public class Automaton {
    private String name;

    public List<Location> getLocations() {
        return locations;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Channel> getActions() {
        return actions;
    }

    public void setName(String name) {
        this.name = name;
    }

    private final List<Location> locations;
    private final List<BoolVar> BVs;
    private final List<Edge> edges;
    private final List<Clock> clocks;
    private Set<Channel> inputAct, outputAct, actions;
    private Location initLoc;

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs) {
        this(name, locations, edges, clocks, BVs, true);
    }

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs, boolean makeInpEnabled) {
        this.name = name;
        this.locations = locations;

        for (Location location : locations) {

            if (location.isInitial()) {
                initLoc = location;
                break;
            }
        }

        this.edges = edges;
        setActions(edges);
        this.clocks = clocks;
        this.BVs = BVs;


        if (makeInpEnabled) {
            CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize);
            CDD.addClocks(clocks);
            CDD.addBddvar(BVs);
            addTargetInvariantToEdges();
            makeInputEnabled();
            CDD.done();
        }
    }

    // Copy constructor
    public Automaton(Automaton copy) {
        this.name = copy.name + "Copy";

        this.clocks = new ArrayList<>();
        for (Clock c : copy.clocks) {
            String[] split = c.getName().split("_");
            this.clocks.add(new Clock(this.name + "_" + split[1]));
        }
        this.BVs = new ArrayList<>();
        for (BoolVar c : copy.BVs) {
            String[] split = c.getName().split("_");
            this.BVs.add(new BoolVar(this.name + "_" + split[1], c.getInitialValue()));   // TODO: Do I need copies? Do I want copies?
        }
        this.locations = new ArrayList<>();
        for (Location loc : copy.locations) {
            this.locations.add(new Location(loc, clocks, copy.clocks, BVs, copy.BVs));
            if (loc.isInitial()) this.initLoc = this.locations.get(this.locations.size() - 1);
        }

        this.edges = new ArrayList<>();
        for (Edge e : copy.edges) {
            int sourceIndex = copy.locations.indexOf(e.getSource());
            int targetIndex = copy.locations.indexOf(e.getTarget());
            this.edges.add(new Edge(e, this.clocks, this.BVs, locations.get(sourceIndex), locations.get(targetIndex), copy.clocks, copy.BVs));
        }

        this.inputAct = copy.inputAct;
        this.outputAct = copy.outputAct;
        this.actions = copy.actions;
    }

    public HashMap<Clock, Integer> getMaxBoundsForAllClocks() {
        HashMap<Clock, Integer> res = new HashMap<Clock, Integer>();

        for (Clock clock : clocks) {
            for (Edge edge : edges) {
                int clockMaxBound = edge.getMaxConstant(clock);
                if (!(res.containsKey(clock)))
                    res.put(clock, clockMaxBound);
                if (clockMaxBound > res.get(clock)) res.put(clock, clockMaxBound);
            }

            for (Location location : locations) {
                int clockMaxBound = location.getMaxConstant(clock);
                if (!(res.containsKey(clock)))
                    res.put(clock, clockMaxBound);
                if (clockMaxBound > res.get(clock))
                    res.put(clock, clockMaxBound);
            }
            if (!res.containsKey(clock) | res.get(clock) == 0)
                res.put(clock, 1);
        }
        return res;
    }


    public String getName() {
        return name;
    }

    private List<Edge> getEdgesFromLocation(Location loc) {
        if (loc.isUniversal()) {
            List<Edge> resultEdges = new ArrayList<>();
            for (Channel action : actions) {
                resultEdges.add(new Edge(loc, loc, action, inputAct.contains(action), new TrueGuard(), new ArrayList<>()));
            }
            return resultEdges;
        }

        return edges.stream().filter(edge -> edge.getSource().equals(loc)).collect(Collectors.toList());
    }

    public List<Edge> getEdgesFromLocationAndSignal(Location loc, Channel signal) {
        List<Edge> resultEdges = getEdgesFromLocation(loc);

        return resultEdges.stream().filter(edge -> edge.getChannel().getName().equals(signal.getName())).collect(Collectors.toList());
    }

    private void setActions(List<Edge> edges) {
        inputAct = new HashSet<>();
        outputAct = new HashSet<>();
        actions = new HashSet<>();

        for (Edge edge : edges) {
            Channel action = edge.getChannel();

            actions.add(action);

            if (edge.isInput()) {
                inputAct.add(action);
            } else {
                outputAct.add(action);
            }
        }
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public Location getInitLoc() {
        return initLoc;
    }

    public Set<Channel> getInputAct() {
        return inputAct;
    }

    public Set<Channel> getOutputAct() {
        return outputAct;
    }

    public List<BoolVar> getBVs() {
        return BVs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Automaton)) return false;
        Automaton automaton = (Automaton) o;

        //System.out.println( name.equals(automaton.name));
        //System.out.println( Arrays.equals(locations.toArray(), automaton.locations.toArray()));
        //System.out.println( Arrays.equals(edges.toArray(), automaton.edges.toArray()));
        //System.out.println( Arrays.equals(clocks.toArray(), automaton.clocks.toArray()) );
        //System.out.println( Arrays.equals(inputAct.toArray(), automaton.inputAct.toArray()));
        //System.out.println(  Arrays.equals(outputAct.toArray(), automaton.outputAct.toArray()) );

        return name.equals(automaton.name) &&
                Arrays.equals(locations.toArray(), automaton.locations.toArray()) &&
                Arrays.equals(edges.toArray(), automaton.edges.toArray()) &&
                Arrays.equals(clocks.toArray(), automaton.clocks.toArray()) &&
                Arrays.equals(BVs.toArray(), automaton.BVs.toArray()) &&
                Arrays.equals(inputAct.toArray(), automaton.inputAct.toArray()) &&
                Arrays.equals(outputAct.toArray(), automaton.outputAct.toArray()) &&
                initLoc.equals(automaton.initLoc);
    }

    @Override
    public String toString() {
        return "Automaton{" +
                "name='" + name + '\'' +
                ", locations=" + Arrays.toString(locations.toArray()) +
                ", edges=" + Arrays.toString(edges.toArray()) +
                ", clocks=" + Arrays.toString(clocks.toArray()) +
                ", BVs=" + Arrays.toString(BVs.toArray()) +
                ", inputAct=" + inputAct +
                ", outputAct=" + outputAct +
                ", actions=" + actions +
                ", initLoc=" + initLoc +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, locations, edges, clocks, BVs, inputAct, outputAct, initLoc);
    }


    public void makeInputEnabled() {
        for (Location loc : getLocations()) {
            System.out.println("now in location " + loc);
            CDD sourceInvariantCDD = loc.getInvariantCDD();
            // loop through all inputs
            for (Channel input : getInputAct()) {

                // build CDD of zones from edges
                List<Edge> inputEdges = getEdgesFromLocationAndSignal(loc, input);
                CDD resCDD;
                CDD cddOfAllEdgesWithCurrentInput = CDD.cddFalse();
                if (!inputEdges.isEmpty()) {
                    for (Edge edge : inputEdges) {
                        CDD target = edge.getTarget().getInvariantCDD();
                        System.out.println("Target CDD: " + target);
                        CDD preGuard1 = target.transitionBack(edge);
                        System.out.println("Pre guard 1: " + preGuard1);
                        cddOfAllEdgesWithCurrentInput = cddOfAllEdgesWithCurrentInput.disjunction(preGuard1);
                        System.out.println("cddOfAllEdgesWithCurrentInput: " + cddOfAllEdgesWithCurrentInput);
                    }

                    // subtract the federation of zones from the original fed
                    resCDD = sourceInvariantCDD.minus(cddOfAllEdgesWithCurrentInput);
                } else {
                    resCDD = sourceInvariantCDD;
                }
                Edge newEdge = new Edge(loc, loc, input, true, CDD.toGuardList(resCDD, getClocks()), new ArrayList<>());
                getEdges().add(newEdge);


            }
        }

    }

    public void addTargetInvariantToEdges() {
        for (Edge edge : getEdges()) {
            CDD targetCDD = edge.getTarget().getInvariantCDD();
            CDD past = targetCDD.transitionBack(edge);
            edge.setGuards(CDD.toGuardList(past.conjunction(edge.getGuardCDD()), getClocks()));
        }
    }

}
