package logic;

import exceptions.CddAlreadyRunningException;
import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Conjunction extends TransitionSystem {
    private final TransitionSystem[] systems;
    private final HashMap<Clock, Integer> maxBounds = new HashMap<>();

    private final HashSet<State> passed = new HashSet<>();
    private final Queue<State> worklist = new ArrayDeque<>();

    private Automaton resultant = null;

    public Conjunction(TransitionSystem... systems)
        throws IllegalArgumentException {
        if (systems.length == 0) {
            throw new IllegalArgumentException("Conjunction can only be done with one or more transition systems");
        }

        this.systems = systems;

        // Initialisation of the underlying TransitionSystem and its components
        for (TransitionSystem system : systems) {
            maxBounds.putAll(system.getMaxBounds());
            clocks.addAll(system.getClocks());
            BVs.addAll(system.getBVs());
        }
    }

    @Override
    public Set<Channel> getInputs() {
        Set<Channel> inputs = new HashSet<>(systems[0].getInputs());
        for (TransitionSystem system : systems) {
            inputs.retainAll(system.getInputs());
        }
        return inputs;
    }

    @Override
    public Set<Channel> getOutputs() {
        Set<Channel> outputs = new HashSet<>(systems[0].getOutputs());
        for (TransitionSystem system : systems) {
            outputs.retainAll(system.getOutputs());
        }
        return outputs;
    }

    @Override
    public List<SimpleTransitionSystem> getSystems() {
        List<SimpleTransitionSystem> result = new ArrayList<>();
        for (TransitionSystem system : systems) {
            result.addAll(system.getSystems());
        }
        return result;
    }

    @Override
    public String getName() {
        String result = "";
        for (TransitionSystem ts: systems)
        {
            result = result + ts.getName() + " && ";
        }
        return result.substring(0,result.length()-4);
    }

    @Override
    public Automaton getAutomaton()
            throws CddAlreadyRunningException {
        // No need for recomputing the same conjunction.
        if (resultant != null) {
            return resultant;
        }

        /* Before creating the conjunction and thereby initialising the CDD.
         *   We must ensure that the underlying operands (Transition systems),
         *   have processed their automaton such that we won't start multiple
         *   CDDs by invoking "GetAutomaton" on the underlying TransitionSystems*/
        Automaton[] automata = new Automaton[systems.length];
        for (int i = 0; i < systems.length; i++) {
            automata[i] = systems[i].getAutomaton();
        }

        /* We utilise a try-finally such that we can correctly clean up whilst still immediately
         *   rethrow the exceptions as we can't handle a failure (most likely from the CDD).
         *   This especially helps increase the meaning of failing tests */
        try {
            resultant = conjoin(automata);
        } finally {
            CDD.done();
        }

        return resultant;
    }

    @Override
    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    @Override
    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = computeResultMoves(locations, channel);
        if (resultMoves.isEmpty()) return new ArrayList<>();
        return createNewTransitions(currentState, resultMoves, allClocks);
    }

    private boolean havePassed(State element) {
        for (State state : passed) {
            if (element.getLocation().getName().equals(state.getLocation().getName()) &&
                    CDD.isSubset(element.getCDD(), state.getCDD())) {
                return true;
            }
        }
        return false;
    }

    private boolean isWaitingFor(State element) {
        for (State state : worklist) {
            if (element.getLocation().getName().equals(state.getLocation().getName()) &&
                    CDD.isSubset(element.getCDD(), state.getCDD())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEdge(Set<Edge> set, Edge edge) {
        for (Edge other : set) {
            if (other.equals(edge) &&
                    other.getGuardCDD().equals(edge.getGuardCDD())) {
                return true;
            }
        }
        return false;
    }

    private String conjoinedAutomataName(Automaton[] automata)
            throws IllegalArgumentException {
        if (automata.length == 0) {
            throw new IllegalArgumentException("Requires at least one automaton to get a conjoined automaton name");
        }
        return String.join(
                " && ",
                Arrays.stream(automata)
                        .map(Automaton::getName)
                        .collect(Collectors.toList())
        );
    }

    private String conjoinedLocationsName(Collection<Location> locations)
            throws IllegalArgumentException {
        if (locations.size() == 0) {
            throw new IllegalArgumentException("Requires at least one location to get a conjoined location name");
        }
        return String.join(
                "",
                locations.stream()
                        .map(Location::getName)
                        .collect(Collectors.toList())
        );
    }

    private List<Location> initialLocations(Automaton[] automata)
            throws IllegalArgumentException {
        if (automata.length == 0) {
            throw new IllegalArgumentException("Requires at least one automaton to get a initial location for the conjunction");
        }
        return Arrays.stream(automata)
                .map(Automaton::getInitial)
                .collect(Collectors.toList());
    }

    private Automaton conjoin(Automaton[] automata)
            throws CddAlreadyRunningException, IllegalArgumentException {
        if (automata.length == 0) {
            throw new IllegalArgumentException("At least a single automaton must be provided for the conjunction");
        }

        CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize);
        CDD.addClocks(getClocks());
        CDD.addBddvar(getBVs());

        Set<Edge> edgesSet = new HashSet<>();
        Set<Location> locationsSet = new HashSet<>();
        Map<String, Location> locationMap = new HashMap<>();

        String name = conjoinedAutomataName(automata);

        // Create the conjunction of all initial locations
        List<Location> initialLocations = initialLocations(automata);
        Location conjoinInitialLocation = conjoinLocation(initialLocations);
        locationsSet.add(conjoinInitialLocation);
        locationMap.put(conjoinInitialLocation.getName(), conjoinInitialLocation);

        Set<Channel> channels = new HashSet<>();
        channels.addAll(getInputs());
        channels.addAll(getOutputs());

        // Initialisation of the worklist with the initial state of the TS
        State initState = getInitialState();
        worklist.add(initState);

        while (!worklist.isEmpty()) {
            State currentState = worklist.remove();
            passed.add(currentState);

            for (Channel channel : channels) {
                List<Transition> transitions = getNextTransitions(currentState, channel, getClocks());

                for (Transition transition : transitions) {
                    /* Get the state following the transition and then extrapolate. If we have not
                     *   already visited the location, this is equivalent to simulating the arrival
                     *   at that location following this transition with the current "channel". */
                    State targetState = transition.getTarget();
                    if (!havePassed(targetState) && !isWaitingFor(targetState)) {
                        targetState.extrapolateMaxBounds(maxBounds, getClocks());
                        worklist.add(targetState);
                    }

                    /* If we don't already have the "targetState" location added
                     *   To the set of locations for the conjunction then add it. */
                    String targetName = targetState.getLocation().getName();
                    locationMap.computeIfAbsent(
                            targetName, key -> {
                                Location newLocation = createLocationFromTargetState(targetState);
                                locationsSet.add(newLocation);
                                return newLocation;
                            }
                    );


                    // Create and add the edge connecting the conjoined locations
                    String sourceName = transition.getSource().getLocation().getName();

                    assert locationMap.containsKey(sourceName);
                    assert locationMap.containsKey(targetName);

                    Edge edge = createEdgeFromTransition(
                            transition,
                            locationMap.get(sourceName),
                            locationMap.get(targetName),
                            channel
                    );
                    if (!containsEdge(edgesSet, edge)) {
                        edgesSet.add(edge);
                    }
                }
            }
        }

        List<Location> updatedLocations = updateClocksInLocs(
                locationsSet, getClocks(), getClocks(), getBVs(), getBVs()
        );
        List<Edge> edgesWithNewClocks = updateClocksInEdges(edgesSet, clocks.getItems(), clocks.getItems(), BVs.getItems(), BVs.getItems());
        Automaton resAut = new Automaton(name, updatedLocations, edgesWithNewClocks, clocks.getItems(), BVs.getItems(), false);
        CDD.done();
        return resAut;
    }

    private Location createLocationFromTargetState(State target) {
        String name = target.getLocation().getName();
        boolean isInitial = target.getLocation().getIsInitial();
        boolean isUrgent = target.getLocation().getIsUrgent();
        boolean isUniversal = target.getLocation().getIsUniversal();
        boolean isInconsistent = target.getLocation().getIsInconsistent();
        Guard invariant = target.getInvariants(clocks.getItems());
        int x = target.getLocation().getX();
        int y = target.getLocation().getX();
        return new Location(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
    }

    private Edge createEdgeFromTransition(Transition transition, Location source, Location target, Channel channel) {
        Guard guard = transition.getGuards(getClocks());
        List<Update> updates = transition.getUpdates();
        boolean isInput = getInputs().contains(channel);
        return new Edge(source, target, channel, isInput, guard, updates);
    }

    private Location conjoinLocation(Collection<Location> locations)
            throws IllegalArgumentException {
        if (locations.size() == 0) {
            throw new IllegalArgumentException("At least a single location is required");
        }

        String name = conjoinedLocationsName(locations);

        CDD invariantFederation = CDD.cddTrue();
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = true;
        boolean isInconsistent = false;
        int x = 0, y = 0;

        for (Location location : locations) {
            invariantFederation = location.getInvariantCDD().conjunction(invariantFederation);
            isInitial = isInitial && location.isInitial();
            isUrgent = isUrgent || location.isUrgent();
            isUniversal = isUniversal && location.isUniversal();
            isInconsistent = isInconsistent || location.isInconsistent();
            x += location.getX();
            y += location.getY();
        }

        // We use the average location coordinates
        x /= locations.size();
        y /= locations.size();

        Guard invariant = CDD.toGuardList(invariantFederation, getClocks());
        return new Location(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<SymbolicLocation> symbolicLocations = ((ComplexLocation) symLocation).getLocations();
        List<Move> result = computeResultMoves(symbolicLocations, channel);
        return result;
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        List<Move> resultMoves = systems[0].getNextMoves(locations.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty()) {
            return new ArrayList<>();
        }

        for (int i = 1; i < systems.length; i++) {
            List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);
            if (channel.getName().contains("coin")) {
                System.out.println(getName());
                System.out.println("Location: " + locations.get(i) + " " + moves.size());
            }
            if (moves.isEmpty()) {
                return new ArrayList<>();
            }
            resultMoves = moveProduct(resultMoves, moves, i == 1, false);
        }

        return resultMoves;
    }
}
