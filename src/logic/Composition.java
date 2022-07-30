package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Composition extends TransitionSystem {
    private final TransitionSystem[] systems;
    private Set<Channel> inputs, outputs;
    private final HashMap<Clock, Integer> maxBounds;
    private List<State> passed = new ArrayList<>();
    private List<State> waiting = new ArrayList<>();


    public Composition(TransitionSystem... systems) {
        this.systems = systems;

        maxBounds = new HashMap<>();
        inputs = new HashSet<>();
        outputs = new HashSet<>();

        // Get all the clocks from all the systems
        clocks.addAll(Arrays.stream(systems)
                .map(TransitionSystem::getClocks)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        // Get all the boolean variables from all the systems
        BVs.addAll(Arrays.stream(systems)
                .map(TransitionSystem::getBVs)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        /* Given a set of specifications we have a composition with:
         * - The Locations is the cartesian product of all specifications
         * - The initial location is the tuple of all initials of all specifications
         * - The actions is the disjoint union of all input/output actions of the specifications
         * - The transition system is later defined.
         * For the composition to be created it must be that:
         * - The intersection of all specification pairs output actions must be the empty set */

        // to compute inputs, outputs and syncs of composed TS, analyse all pairs of TS's
        for (int i = 0; i < systems.length; i++) {
            for (int j = 0; j < systems.length; j++) {
                if (i == j) {
                    continue;
                }

                TransitionSystem system_i = systems[i];
                TransitionSystem system_j = systems[j];

                // Same output actions check
                Set<Channel> output_intersection = intersect(
                        system_i.getOutputs(),
                        system_j.getOutputs()
                );
                if (!output_intersection.isEmpty()) {
                    throw new IllegalArgumentException("The output actions of all pairs of specifications must have an empty intersection");
                }

                // Act_o = Act_o^i U Act_o^j
                outputs = union(
                        outputs, system_j.getOutputs()
                );

                // Act_i = (Act_i^i \ Act_o^j) U (Act_i^j \ Act_o^i)
                inputs = union(
                        difference(inputs, system_j.getOutputs()),
                        difference(system_j.getInputs(), outputs)
                );
            }
        }

        // Sets the max bounds
        for (TransitionSystem system : systems) {
            maxBounds.putAll(
                    system.getMaxBounds()
            );
        }
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public Set<Channel> getSyncs() {
        return new HashSet<>();
    }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<SimpleTransitionSystem> getSystems() {
        return Arrays.stream(systems)
                .map(TransitionSystem::getSystems)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Automaton createComposition(List<Automaton> autList) {
        CDD.init(CDD.maxSize, CDD.cs, CDD.stackSize);
        CDD.addClocks(getClocks());
        CDD.addBooleans(BVs.getItems());
        String name = "";

        Set<Edge> edgesSet = new HashSet<>();
        Set<Location> locationsSet = new HashSet<>();
        Map<String, Location> locMap = new HashMap<>();
        passed = new ArrayList<>();
        waiting = new ArrayList<>();

        List<Location> initLoc = new ArrayList<>();
        for (Automaton aut : autList) {
            initLoc.add(aut.getInitial());
            if (name.isEmpty())
                name = aut.getName();
            else
                name += " | " + aut.getName();
        }

        Location initL = createLoc(initLoc);
        locationsSet.add(initL);

        Set<Channel> all = new HashSet<>();
        all.addAll(outputs);
        all.addAll(inputs);


        locMap.put(initL.getName(), initL);

        State initState = getInitialState();
        ;
        waiting.add(initState);

        while (!waiting.isEmpty()) {
            State currentState = (State) waiting.toArray()[0];
            waiting.remove(currentState);
            passed.add(currentState);

            for (Channel chan : all) {

                List<Transition> transList = getNextTransitions(currentState, chan, clocks.getItems());
                for (Transition trans : transList) {
                    String targetName = trans.getTarget().getLocation().getName();

                    boolean isInitial = trans.getTarget().getLocation().getIsInitial();
                    boolean isUrgent = trans.getTarget().getLocation().getIsUrgent();
                    boolean isUniversal = trans.getTarget().getLocation().getIsUniversal();
                    boolean isInconsistent = trans.getTarget().getLocation().getIsInconsistent();
                    Guard invariant = trans.getTarget().getInvariants(clocks.getItems());
                    String sourceName = trans.getSource().getLocation().getName();
                    int x = trans.getTarget().getLocation().getX();
                    int y = trans.getTarget().getLocation().getX();

                    Location target;
                    if (locMap.containsKey(targetName))
                        target = locMap.get(targetName);
                    else {
                        target = new Location(targetName, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
                        locMap.put(targetName, target);
                    }
                    locationsSet.add(target);
                    if (!passedContains(trans.getTarget()) && !waitingContains(trans.getTarget())) {
                        trans.getTarget().extrapolateMaxBounds(maxBounds, clocks.getItems());
                        waiting.add(trans.getTarget());
                    }
                    Guard guardList = trans.getGuards(clocks.getItems()); // TODO: Check!
                    List<Update> updateList = trans.getUpdates();
                    boolean isInput = false;
                    if (inputs.contains(chan))
                        isInput = true;
                    assert (locMap.get(sourceName) != null);
                    assert (locMap.get(targetName) != null);

                    Edge e = new Edge(locMap.get(sourceName), locMap.get(targetName), chan, isInput, guardList, updateList);
                    boolean edgeAlreadyExists = false;
                    for (Edge otherE : edgesSet) {
                        if (otherE.getSource().equals(e.getSource()) && otherE.getTarget().equals(e.getTarget()) && otherE.getChannel().equals(e.getChannel()) && e.isInput() == otherE.isInput() && Arrays.equals(Arrays.stream(e.getUpdates().toArray()).toArray(), Arrays.stream(otherE.getUpdates().toArray()).toArray())) {

                            if (e.getGuardCDD().equiv(otherE.getGuardCDD())) ;
                            {

                                edgeAlreadyExists = true;
                            }
                        }
                    }
                    if (!edgeAlreadyExists)
                        edgesSet.add(e);

                }


            }

        }

        List<Location> locsWithNewClocks = updateClocksInLocs(locationsSet, clocks.getItems(), clocks.getItems(), BVs.getItems(), BVs.getItems());
        List<Edge> edgesWithNewClocks = updateClocksInEdges(edgesSet, clocks.getItems(), clocks.getItems(), BVs.getItems(), BVs.getItems());
        Automaton resAut = new Automaton(name, locsWithNewClocks, edgesWithNewClocks, clocks.getItems(), BVs.getItems(), false);
        CDD.done();
        return resAut;

    }

    public boolean passedContains(State s) {
        boolean contained = false;

        for (State st : passed.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList())) {
            if (s.getInvariant().isSubset(st.getInvariant()))
                contained = true;
        }
        return contained;
    }

    public boolean waitingContains(State s) {
        boolean contained = false;

        for (State st : waiting.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList())) {

            if (s.getInvariant().isSubset(st.getInvariant())) {
                contained = true;
            }
        }
        return contained;
    }

    public Location createLoc(List<Location> locList) {
        String name = "";
        Guard invariant;

        CDD invarFed = CDD.cddUnrestrained();
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = false;
        boolean isInconsistent = false;
        int x = 0, y = 0;

        for (Location l : locList) {
            if (name.isEmpty())
                name = l.getName();
            else
                name += "" + l.getName();

            invarFed = l.getInvariantCDD().conjunction(invarFed);
            isInitial = isInitial && l.isInitial();
            isUrgent = isUrgent || l.isUrgent();
            isUniversal = isUniversal && l.isUniversal(); // TODO: double check this at some point.
            isInconsistent = isInconsistent || l.isInconsistent();
            x += l.getX();
            y += l.getY();

        }
        invariant = invarFed.getGuard(clocks.getItems());

        return new Location(name, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x / locList.size(), y / locList.size());

    }


    @Override
    public Automaton getAutomaton() {

        List<Automaton> autList = new ArrayList<>();
        for (int i = 0; i < systems.length; i++)
            autList.add(systems[i].getAutomaton());

        Automaton resAut = createComposition(autList);
        return resAut;


    }

    // build a list of transitions from a given state and a signal
    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {

        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = new ArrayList<>();

        if (checkForOutputs(channel, locations))
            resultMoves = computeResultMoves(locations, channel);
        List<Transition> transitions = createNewTransitions(currentState, resultMoves, allClocks);
        return transitions;
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        // Check if action belongs to this TS at all before proceeding
        if (!outputs.contains(channel) && !inputs.contains(channel))
            return new ArrayList<>();
        //System.out.println(symLocation.toString());
        //System.out.println(systems[0].getAutomaton().getName());
        //System.out.println(systems[1].getAutomaton().getName());
        // If action is sync, then check if there is corresponding output in TS
        if (!checkForOutputs(channel, ((ComplexLocation) symLocation).getLocations())) return new ArrayList<>();

        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        return computeResultMoves(symLocs, channel);
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        boolean moveExisted = false;

        List<Move> resultMoves = systems[0].getNextMoves(locations.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            resultMoves = new ArrayList<>(Collections.singletonList(new Move(locations.get(0), locations.get(0), new ArrayList<>())));
        else
            moveExisted = true;


        for (int i = 1; i < systems.length; i++) {
            List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);

            if (moves.isEmpty())
                moves = new ArrayList<>(Collections.singletonList(new Move(locations.get(i), locations.get(i), new ArrayList<>())));
            else
                moveExisted = true;

            resultMoves = moveProduct(resultMoves, moves, i == 1, false);
        }

        if (!moveExisted) return new ArrayList<>();
        return resultMoves;
    }

    private boolean checkForOutputs(Channel channel, List<SymbolicLocation> locations) {
        // loop through all automata to find the one sending the output
        for (int i = 0; i < systems.length; i++) {
            /* Here "i" is not only the index of the system,
             *   but the order of the locations must also follow
             *   the one of the composition meaning that the i'th
             *   location is also for the i'th system. */
            TransitionSystem system = systems[i];
            SymbolicLocation location = locations.get(i);

            if (system.getOutputs().contains(channel) &&
                    !system.hasMove(location, channel)) {
                // do not check for outputs if the state in the corresponding automaton does not send that output
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        String result = "";
        for (TransitionSystem ts : systems) {
            result = result + ts.getName() + " || ";
        }
        return result.substring(0, result.length() - 4);
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