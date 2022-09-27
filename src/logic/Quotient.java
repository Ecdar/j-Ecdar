package logic;

import log.Log;
import models.*;

import java.util.*;

public class Quotient extends TransitionSystem {
    private final TransitionSystem t, s;
    private final Set<Channel> inputs, outputs;
    private final Channel newChan;
    private Clock newClock;

    private final HashMap<Clock, Integer> maxBounds = new HashMap<>();
    private final HashSet<State> passed = new HashSet<>();
    private final Queue<State> worklist = new ArrayDeque<>();

    public Quotient(TransitionSystem t, TransitionSystem s) {
        this.t = t;
        this.s = s;

        //clocks should contain the clocks of ts1, ts2 and a new clock
        newClock = new Clock("quo_new", "quo"); //TODO: get ownerName in a better way
        clocks.add(newClock);
        clocks.addAll(t.getClocks());
        clocks.addAll(s.getClocks());
        BVs.addAll(t.getBVs());
        BVs.addAll(s.getBVs());

        // Act_i = Act_i^T ∪ Act_o^S
        inputs = union(t.getInputs(), s.getOutputs());
        newChan = new Channel("i_new");
        inputs.add(newChan);

        // Act_o = Act_o^T \ Act_o^S ∪ Act_i^S \ Act_i^T
        outputs = union(
                difference(t.getOutputs(), s.getOutputs()),
                difference(s.getInputs(), t.getInputs())
        );

        maxBounds.putAll(t.getMaxBounds());
        maxBounds.putAll(s.getMaxBounds());
    }

    @Override
    public Automaton getAutomaton() {
        return calculateQuotientAutomaton().getAutomaton();
    }

    public SymbolicLocation getInitialLocation() {
        // the invariant of locations consisting of locations from each transition system should be true
        // which means the location has no invariants
        return getInitialLocation(new TransitionSystem[]{t, s});
    }

    public SimpleTransitionSystem calculateQuotientAutomaton() {
        return calculateQuotientAutomaton(false);
    }

    public SimpleTransitionSystem calculateQuotientAutomaton(boolean prepareForBisimilarityReduction) {
        boolean initialisedCdd = CDD.tryInit(getClocks(), BVs.getItems());

        String name = getName();

        Set<Edge> edges = new HashSet<>();
        Set<Location> locations = new HashSet<>();
        Map<String, Location> locationMap = new HashMap<>();

        State initialState = getInitialState();
        Location initial = fromSymbolicLocation(initialState.getLocation());
        locations.add(initial);
        locationMap.put(initial.getName(), initial);

        Set<Channel> channels = new HashSet<>();
        channels.addAll(getOutputs());
        channels.addAll(getInputs());

        worklist.add(
                initialState
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
                                Location newLocation = new Location(targetState, clocks.getItems());
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

        return new SimpleTransitionSystem(resAut);
    }

    private Location fromSymbolicLocation(SymbolicLocation location) {
        return new Location(
                location.getName(),
                location.getInvariantGuard(),
                location.isInitial(),
                location.isUrgent(),
                location.isUniversal(),
                location.isInconsistent(),
                location.getX(),
                location.getY()
        );
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
        Guard guard = transition.getGuards(getClocks());
        List<Update> updates = transition.getUpdates();
        boolean isInput = getInputs().contains(channel);
        return new Edge(source, target, channel, isInput, guard, updates);
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public List<SimpleTransitionSystem> getSystems() {
        // no idea what this is for
        List<SimpleTransitionSystem> result = new ArrayList<>();
        result.addAll(t.getSystems());
        result.addAll(s.getSystems());
        return result;
    }

    @Override
    public String getName() {
        return t.getName() + "//" + s.getName();
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        // get possible transitions from current state, for a given channel
        SymbolicLocation location = currentState.getLocation();
        List<Move> moves = getNextMoves(location, channel);
        return createNewTransitions(currentState, moves, allClocks);
    }

    public List<Move> getNextMoves(SymbolicLocation location, Channel a) {
        SymbolicLocation univ = SymbolicLocation.createUniversalLocation("universal", 0, 0);
        SymbolicLocation inc = SymbolicLocation.createInconsistentLocation("inconsistent", 0, 0);

        List<Move> resultMoves = new ArrayList<>();
        /*Log.debug("gettingNextMove of " + location.getName());
        Log.debug("Universal? " + location.getIsUniversal() + " instance of? " + (location instanceof UniversalLocation));
        Log.debug("Inconsistent? " + location.getIsInconsistent() + " instance of? " + (location instanceof InconsistentLocation));
        assert location.getIsUniversal() == (location instanceof UniversalLocation);
        assert location.getIsInconsistent() == (location instanceof InconsistentLocation);*/

        // Rule 10
        if (location.isInconsistent()) {
            if (getInputs().contains(a)) {
                Log.debug("Rule 10");
                Move newMove = new Move(location, inc, new ArrayList<>());
                newMove.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMove);
            }
        }

        // Rule 9
        if (location.isUniversal()) {
            if (getActions().contains(a)) {
                Log.debug("Rule 9");
                Move newMove = new Move(location, univ, new ArrayList<>());
                resultMoves.add(newMove);
            }
        }

        if (location.isProduct()) {
            List<SymbolicLocation> locations = location.getProductOf();

            // symbolic locations corresponding to each TS
            SymbolicLocation lt = locations.get(0);
            SymbolicLocation ls = locations.get(1);

            List<Move> t_moves = t.getNextMoves(lt, a);
            List<Move> s_moves = s.getNextMoves(ls, a);

            // rule 1 (cartesian product)
            if (in(a, intersect(s.getActions(), t.getActions()))) {
                Log.debug("Rule 1");
                List<Move> moveProduct = moveProduct(t_moves, s_moves, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // rule 2
            if (in(a, difference(s.getActions(), t.getActions()))) {
                Log.debug("Rule 2");
                List<Move> movesLeft = new ArrayList<>();
                movesLeft.add(new Move(lt,lt, new ArrayList<>()));

                List<Move> moveProduct = moveProduct(movesLeft, s_moves, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // rule 3
            // rule 4
            // rule 5
            if (in(a, s.getOutputs())) {
                Log.debug("Rule 345 1");
                CDD guard_s = CDD.cddFalse();
                for (Move s_move : s_moves) {
                    guard_s = guard_s.disjunction(s_move.getEnabledPart());
                }
                guard_s = guard_s.negation().removeNegative().reduce();

                CDD inv_neg_inv_loc_s = ls.getInvariantCddNew().negation().removeNegative().reduce();

                CDD combined = guard_s.disjunction(inv_neg_inv_loc_s);

                Move move = new Move(location, univ, new ArrayList<>());
                move.conjunctCDD(combined);
                resultMoves.add(move);
            } else {
                Log.debug("Rule 345 2");
                CDD inv_neg_inv_loc_s = ls.getInvariantCddNew().negation().removeNegative().reduce();

                Move move = new Move(location, univ);
                move.conjunctCDD(inv_neg_inv_loc_s);
                resultMoves.add(move);
            }

            // rule 6
            if (in(a, intersect(t.getOutputs(), s.getOutputs()))) {
                Log.debug("Rule 6");
                // take all moves from left in order to gather the guards and negate them
                CDD CDDFromMovesFromLeft = CDD.cddFalse();
                for (Move moveLeft : t_moves) {
                    CDDFromMovesFromLeft = CDDFromMovesFromLeft.disjunction(moveLeft.getEnabledPart());
                }
                CDD negated = CDDFromMovesFromLeft.negation().removeNegative().reduce();


                for (Move move : s_moves) {
                    Move newMoveRule6 = new Move(location, inc, new ArrayList<>());
                    newMoveRule6.setGuards(move.getEnabledPart().conjunction(negated));
                    newMoveRule6.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                    resultMoves.add(newMoveRule6);
                }
            }

            // rule 7
            if (Objects.equals(a.getName(), this.newChan.getName())) {
                Log.debug("Rule 7");
                Move newMoveRule7 = new Move(location, inc, new ArrayList<>());
                // invariant is negation of invariant of left conjuncted with invariant of right
                CDD negatedInvar = lt.getInvariantCddNew().negation();
                CDD combined = negatedInvar.conjunction(ls.getInvariantCddNew());

                newMoveRule7.setGuards(combined);
                newMoveRule7.setUpdates(new ArrayList<>(Collections.singletonList(new ClockUpdate(newClock, 0))));
                resultMoves.add(newMoveRule7);
            }

            // rule 8
            if (in(a, difference(t.getActions(), s.getActions()))) {
                Log.debug("Rule 8");
                List<Move> movesRight = new ArrayList<>();
                movesRight.add(new Move(ls,ls,new ArrayList<>()));
                List<Move> moveProduct = moveProduct(t_moves, movesRight, true,true);
                for (Move move : moveProduct) {
                    move.conjunctCDD(move.getEnabledPart());
                }
                resultMoves.addAll(moveProduct);
            }

            // Rule 10
        }

        return resultMoves;
    }

    private boolean in(Channel element, Set<Channel> set) {
        return set.contains(element);
    }

    private boolean disjoint(Set<Channel> set1, Set<Channel> set2) {
        return empty(intersect(set1, set2));
    }

    private boolean empty(Set<Channel> set) {
        return set.isEmpty();
    }

    private Set<Channel> intersect(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    private Set<Channel> difference(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        return difference;
    }

    private Set<Channel> union(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }
}
