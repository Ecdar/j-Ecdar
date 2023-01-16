package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AggregatedTransitionSystem extends TransitionSystem {
    protected final TransitionSystem[] systems;
    private final HashMap<Clock, Integer> maxBounds = new HashMap<>();

    private final HashSet<State> passed = new HashSet<>();
    private final Queue<State> worklist = new ArrayDeque<>();

    private Automaton resultant = null;

    public AggregatedTransitionSystem(TransitionSystem... systems)
            throws IllegalArgumentException {
        if (systems.length == 0) {
            throw new IllegalArgumentException("Aggregated transition system must consists of least one transition system");
        }

        this.systems = systems;

        for (TransitionSystem system : systems) {
            clocks.addAll(
                    system.getClocks()
            );
            BVs.addAll(
                    system.getBVs()
            );
            maxBounds.putAll(
                    system.getMaxBounds()
            );
        }
    }

    @Override
    public List<SimpleTransitionSystem> getSystems() {
        return Arrays.stream(systems)
                .map(TransitionSystem::getSystems)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Location getInitialLocation() {
        return getInitialLocation(systems);
    }

    @Override
    public Automaton getAutomaton() {
        // No need for recomputing the same composition
        if (resultant != null) {
            return resultant;
        }

        /* Before creating the composition and thereby initialising the CDD.
         *   We must ensure that the underlying operands (Transition systems),
         *   have processed their automaton such that we won't start multiple
         *   CDDs by invoking "GetAutomaton" on the underlying TransitionSystems */
        Automaton[] automata = new Automaton[systems.length];
        for (int i = 0; i < systems.length; i++) {
            automata[i] = systems[i].getAutomaton();
        }

        /* We utilise a try-finally such that we can correctly clean up whilst still immediately
         *   rethrow the exceptions as we can't handle a failure (most likely from the CDD).
         *   This especially helps increase the meaning of failing tests */
        try {
            resultant = aggregate(automata);
        } finally {
            CDD.done();
        }

        return resultant;
    }

    @Override
    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        return createNewTransitions(
                currentState, getNextMoves(currentState.getLocation(), channel), allClocks
        );
    }

    @Override
    public List<Move> getNextMoves(Location location, Channel channel) {
        // Check if action belongs to this transition system at all before proceeding
        if (!getOutputs().contains(channel) && !getInputs().contains(channel)) {
            return new ArrayList<>();
        }

        // Check that the location is ComplexLocation
        if (!location.isComposed()) {
            throw new IllegalArgumentException(
                    "The location type must be ComplexLocation as aggregated transition systems requires multiple locations"
            );
        }
        List<Location> locations = location.getChildren();

        /* Check that the complex locations size is the same as the systems
         * This is because the index of the system,
         *   determines also the location. Meaning that,
         *   the i'th system has the i'th location. */
        if (locations.size() != getRootSystems().size()) {
            throw new IllegalStateException(
                    "The amount of locations in the complex location must be exactly the same as the amount of systems"
            );
        }

        return computeResultMoves(locations, channel);
    }

    protected List<Move> computeResultMoves(List<Location> locations, Channel channel) {
        return new ArrayList<>();
    }

    protected List<TransitionSystem> getRootSystems() {
        return Arrays.asList(systems);
    }

    protected boolean in(Channel element, Set<Channel> set) {
        return set.contains(element);
    }

    protected Set<Channel> intersect(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    protected Set<Channel> difference(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        return difference;
    }

    protected Set<Channel> union(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }

    private Automaton aggregate(Automaton[] automata) {
        boolean initialisedCdd = CDD.tryInit(getClocks(), BVs.getItems());

        String name = getName();

        Set<Edge> edges = new HashSet<>();
        Set<Location> locations = new HashSet<>();
        Map<String, Location> locationMap = new HashMap<>();

        State initialState = getInitialState();
        Location initial = initialState.getLocation();
        locations.add(initial);
        locationMap.put(initial.getName(), initial);

        Set<Channel> channels = new HashSet<>();
        channels.addAll(getOutputs());
        channels.addAll(getInputs());

        worklist.add(
                getInitialState()
        );

        while (!worklist.isEmpty()) {
            State state = worklist.remove();
            passed.add(state);

            for (Channel channel : channels) {
                List<Transition> transitions = getNextTransitions(state, channel, clocks.getItems());

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
                                Location newLocation = Location.createFromState(targetState, clocks.getItems());
                                locations.add(newLocation);
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
                    if (!containsEdge(edges, edge)) {
                        edges.add(edge);
                    }
                }
            }
        }

        List<Location> updatedLocations = updateLocations(
                locations, getClocks(), getClocks(), getBVs(), getBVs()
        );
        List<Edge> edgesWithNewClocks = updateEdges(edges, clocks.getItems(), clocks.getItems(), BVs.getItems(), BVs.getItems());
        Automaton resAut = new Automaton(name, updatedLocations, edgesWithNewClocks, clocks.getItems(), BVs.getItems(), false);

        if (initialisedCdd) {
            CDD.done();
        }

        return resAut;
    }

    private boolean havePassed(State element) {
        for (State state : passed) {
            if (element.getLocation().getName().equals(state.getLocation().getName()) &&
                    element.getInvariant().isSubset(state.getInvariant())) {
                return true;
            }
        }
        return false;
    }

    private boolean isWaitingFor(State element) {
        for (State state : worklist) {
            if (element.getLocation().getName().equals(state.getLocation().getName()) &&
                    element.getInvariant().isSubset(state.getInvariant())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEdge(Set<Edge> set, Edge edge) {
        return set.stream().anyMatch(other -> other.equals(edge) &&
                other.getGuardCDD().equals(edge.getGuardCDD())
        );
    }

    private Edge createEdgeFromTransition(Transition transition, Location source, Location target, Channel channel) {
        Expression guard = transition.getGuard(getClocks());
        List<Update> updates = transition.getUpdates();
        boolean isInput = getInputs().contains(channel);
        return new Edge(source, target, channel, isInput, guard, updates);
    }
}
