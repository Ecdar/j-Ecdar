package models;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

public class Automaton {
    private String name;

    private final List<Location> locations;
    private final List<BoolVar> BVs;
    private final List<Edge> edges;
    private final List<Clock> clocks;
    private Set<Channel> inputAct, outputAct, actions;
    private final Location initial;

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs, boolean makeInputEnabled) {
        this.name = name;
        this.locations = locations;
        this.clocks = clocks;
        this.BVs = BVs;

        List<Location> initialLocations = locations.stream()
                .filter(Location::isInitial)
                .collect(Collectors.toList());
        if (initialLocations.size() > 1) {
            throw new IllegalArgumentException("Cannot have more than one initial location");
        }
        if (initialLocations.size() == 0) {
            throw new IllegalArgumentException("Must have one initial location");
        }
        initial = initialLocations.get(0);

        this.edges = edges;

        inputAct = new HashSet<>();
        outputAct = new HashSet<>();
        for (Edge edge : this.edges) {
            Channel action = edge.getChannel();
            if (edge.isInput()) {
                this.inputAct.add(action);
            } else {
                this.outputAct.add(action);
            }
        }

        actions = Sets.newHashSet(
                Iterables.concat(inputAct, outputAct)
        );

        if (makeInputEnabled) {
            CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize);
            CDD.addClocks(clocks);
            CDD.addBddvar(BVs);
            addTargetInvariantToEdges();

            makeInputEnabled();

            CDD.done();
        }
    }

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs) {
        this(name, locations, edges, clocks, BVs, true);
    }

    public Automaton(Automaton automaton) {
        name = automaton.name + "Copy";
        clocks = automaton.clocks.stream()
                .map(clock -> new Clock(clock.getOriginalName() + "Copy", name))
                .collect(Collectors.toList());
        BVs = automaton.BVs.stream()
                .map(boolVar -> new BoolVar(boolVar.getOriginalName() + "Copy", name, boolVar.getInitialValue()))
                .collect(Collectors.toList());
        locations = automaton.locations.stream()
                .map(location -> new Location(location, clocks, automaton.clocks, BVs, automaton.BVs))
                .collect(Collectors.toList());
        edges = automaton.edges.stream()
                .map(edge -> {
                    int sourceIndex = automaton.locations.indexOf(edge.getSource());
                    int targetIndex = automaton.locations.indexOf(edge.getTarget());
                    Location source = locations.get(sourceIndex);
                    Location target = locations.get(targetIndex);
                    return new Edge(edge, clocks, BVs, source, target, automaton.clocks, automaton.BVs);
                }).collect(Collectors.toList());
        inputAct = automaton.inputAct;
        outputAct = automaton.outputAct;
        actions = automaton.actions;
        initial = automaton.initial;
    }

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

    public String getName() {
        return name;
    }

    public List<Clock> getClocks() {
        return clocks;
    }

    public Location getInitial() {
        return initial;
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

    public HashMap<Clock, Integer> getMaxBoundsForAllClocks() {
        HashMap<Clock, Integer> result = new HashMap<>();

        for (Clock clock : clocks) {
            for (Edge edge : edges) {
                result.compute(
                        clock,
                        (key, value) -> {
                            int clockMaxBound = edge.getMaxConstant(clock);
                            return Math.max(value == null ? clockMaxBound : value, clockMaxBound);
                        }
                );
            }

            for (Location location : locations) {
                result.compute(
                        clock,
                        (key, value) -> {
                            int clockMaxBound = location.getMaxConstant(clock);
                            return Math.max(value == null ? clockMaxBound : value, clockMaxBound);
                        }
                );
            }

            if (!result.containsKey(clock) || result.get(clock) == 0) {
                result.put(clock, 1);
            }
        }
        return result;
    }

    private List<Edge> getEdgesFromLocation(Location loc) {
        if (loc.isUniversal()) {
            return actions.stream()
                    .map(action -> new Edge(loc, loc, action, inputAct.contains(action), new TrueGuard(), new ArrayList<>()))
                    .collect(Collectors.toList());
        }
        return edges.stream().filter(edge -> edge.getSource().equals(loc)).collect(Collectors.toList());
    }

    public List<Edge> getEdgesFromLocationAndSignal(Location loc, Channel signal) {
        List<Edge> resultEdges = getEdgesFromLocation(loc);
        return resultEdges.stream()
                .filter(edge -> edge.getChannel().getName().equals(signal.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Automaton)) {
            return false;
        }

        Automaton automaton = (Automaton) o;

        return name.equals(automaton.name) &&
                Arrays.equals(locations.toArray(), automaton.locations.toArray()) &&
                Arrays.equals(edges.toArray(), automaton.edges.toArray()) &&
                Arrays.equals(clocks.toArray(), automaton.clocks.toArray()) &&
                Arrays.equals(BVs.toArray(), automaton.BVs.toArray()) &&
                Arrays.equals(inputAct.toArray(), automaton.inputAct.toArray()) &&
                Arrays.equals(outputAct.toArray(), automaton.outputAct.toArray()) &&
                initial.equals(automaton.initial);
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
                ", initLoc=" + initial +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, locations, edges, clocks, BVs, inputAct, outputAct, initial);
    }

    public void makeInputEnabled() {
        for (Location loc : getLocations()) {
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
                        CDD preGuard1 = target.transitionBack(edge);
                        cddOfAllEdgesWithCurrentInput = cddOfAllEdgesWithCurrentInput.disjunction(preGuard1);
                    }
                    cddOfAllEdgesWithCurrentInput = cddOfAllEdgesWithCurrentInput.removeNegative().reduce();
                    // subtract the federation of zones from the original fed
                    resCDD = sourceInvariantCDD.minus(cddOfAllEdgesWithCurrentInput);
                } else {
                    resCDD = sourceInvariantCDD;
                }

                if (resCDD.isNotFalse()) {
                    Edge newEdge = new Edge(loc, loc, input, true, CDD.toGuardList(resCDD, getClocks()), new ArrayList<>());
                    getEdges().add(newEdge);
                }

            }
        }

    }

    public void addTargetInvariantToEdges() {
        for (Edge edge : getEdges()) {
            CDD targetCDD = edge.getTarget().getInvariantCDD();
            CDD past = targetCDD.transitionBack(edge);
            if (!past.equiv(CDD.cddTrue()))
                edge.setGuard(CDD.toGuardList(past.conjunction(edge.getGuardCDD()), getClocks()));
        }
    }
}