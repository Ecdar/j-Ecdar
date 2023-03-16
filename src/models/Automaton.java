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
    private final Set<Channel> inputAct, outputAct, actions;
    private final Location initial;

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs, boolean makeInputEnabled) {
        if (locations.isEmpty()) {
            throw new IllegalArgumentException(String.format("Automaton %s must have at least one location.", name));
        }

        this.name = name;
        this.locations = locations;
        this.clocks = clocks;
        this.BVs = BVs;

        List<Location> initialLocations = locations.stream()
                .filter(Location::isInitial)
                .collect(Collectors.toList());
        if (initialLocations.size() > 1) {
            throw new IllegalArgumentException(String.format("Automaton %s cannot have more than one initial location", name));
        }
        if (initialLocations.size() == 0) {
            throw new IllegalArgumentException(String.format("Automaton %s must have at least one initial location", name));
        }
        initial = initialLocations.get(0);

        this.edges = edges;

        // Retrieve the inputs and outputs
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

        /* The finite set of actions must be partitioned into inputs and outputs.
         *   Here we check whether they are partitioned by checking that the intersection of the
         *   inputs and outputs are empty as an action can only be either an input xor an output.
         *   We don't have to check whether an action is neither an input nor an output as the set
         *   of actions is build as the union of the inputs and outputs and for this reason
         *   guarantees to be in the set of actions. */
        Set<Channel> intersection = new HashSet<>(inputAct);
        intersection.retainAll(outputAct);
        if (!intersection.isEmpty()) {
            // Constructs a string with the names of the actions violating the partition property as "{a, b, c}"
            String violatingActions = "{" + intersection
                    .stream()
                    .map(Channel::getName)
                    .collect(Collectors.joining(", ")) + "}";
            throw new IllegalArgumentException(String.format("The actions %s of specification automaton %s is not a partition.", violatingActions, name));
        }

        /* Since inputAct and outputAct are now disjoint, we can simply construct actions as the union of both sets. */
        actions = Sets.newHashSet(
                Iterables.concat(inputAct, outputAct)
        );

        if (makeInputEnabled) {
            addTargetInvariantToEdges();
            makeInputEnabled();
        }
    }

    public Automaton(String name, List<Location> locations, List<Edge> edges) {
        this(
                name,
                locations,
                edges,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public Automaton(String name, Location location, List<Edge> edges, List<Clock> clocks, List<BoolVar> booleans) {
        this(
                name,
                Collections.singletonList(location),
                edges,
                clocks,
                booleans
        );
    }

    public Automaton(String name, Location location, List<Edge> edges) {
        this(
                name,
                Collections.singletonList(location),
                edges
        );
    }

    public Automaton(String name, List<Location> locations, List<Edge> edges, List<Clock> clocks, List<BoolVar> BVs) {
        this(name, locations, edges, clocks, BVs, true);
    }

    public Automaton(Automaton automaton) {
        name = automaton.name + "Copy";
        clocks = automaton.clocks.stream()
                .map(clock -> new Clock(clock.getOriginalName() + "Copy", name, clock.isGlobal()))
                .collect(Collectors.toList());
        BVs = automaton.BVs.stream()
                .map(boolVar -> new BoolVar(boolVar.getOriginalName() + "Copy", name, boolVar.getInitialValue()))
                .collect(Collectors.toList());
        locations = automaton.locations.stream()
                .map(location -> location.copy(clocks, automaton.clocks, BVs, automaton.BVs))
                .collect(Collectors.toList());
        edges = automaton.edges.stream()
                .map(edge -> {
                    int sourceIndex = automaton.locations.indexOf(edge.getSource());
                    int targetIndex = automaton.locations.indexOf(edge.getTarget());
                    Location source = locations.get(sourceIndex);
                    Location target = locations.get(targetIndex);
                    return new Edge(edge, clocks, BVs, source, target, automaton.clocks, automaton.BVs);
                }).collect(Collectors.toList());
        inputAct = new HashSet<>(automaton.inputAct);
        outputAct = new HashSet<>(automaton.outputAct);
        actions = new HashSet<>(automaton.actions);
        initial = automaton.initial.copy(clocks, automaton.clocks, BVs, automaton.BVs);
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
        boolean initialisedCdd = CDD.tryInit(clocks, BVs);

        for (Location location : getLocations()) {
            CDD invariant = location.getInvariantCdd();

            for (Channel input : getInputAct()) {
                // The part which is already handled by existing edges.
                CDD enabledPart = CDD.cddFalse();
                // The part which requires an edge to be input enabled.
                CDD disabledPart = invariant;

                // Calculate the enabled CDD.
                List<Edge> edges = getEdgesFromLocationAndSignal(location, input);
                for (Edge edge : edges) {
                    CDD targetInvariant = edge.getTarget().getInvariantCdd();
                    CDD preGuard = targetInvariant.transitionBack(edge);
                    enabledPart = enabledPart.disjunction(preGuard);
                }

                // If the enabled part is true then the disabled part will be false.
                if (enabledPart.isTrue()) {
                    continue;
                }

                // If there is any solution to the enabled CDD then subtract it from the invariant.
                if (enabledPart.isNotFalse()) {
                    disabledPart = disabledPart.minus(enabledPart);
                }

                // If there is any solution to the disabled CDD then create an edge.
                if (disabledPart.isNotFalse()) {
                    Edge newEdge = new Edge(location, location, input, true, disabledPart.getGuard(), new ArrayList<>());
                    getEdges().add(newEdge);
                }
            }
        }

        if (initialisedCdd) {
            CDD.done();
        }
    }

    public void addTargetInvariantToEdges() {
        boolean initialisedCdd = CDD.tryInit(clocks, BVs);

        for (Edge edge : getEdges()) {
            CDD targetCDD = CDDFactory.create(edge.getTarget().getInvariantGuard());
            CDD past = targetCDD.transitionBack(edge);
            if (!past.equiv(CDD.cddTrue()))
                edge.setGuard(past.conjunction(edge.getGuardCDD()).getGuard(getClocks()));
        }

        if (initialisedCdd) {
            CDD.done();
        }
    }
}