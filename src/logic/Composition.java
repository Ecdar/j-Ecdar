package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Composition extends TransitionSystem {
    private final TransitionSystem[] systems;
    private final Set<Channel> inputs, outputs, syncs;
    private List<Refinement.State> passed = new ArrayList<>();
    private List<Refinement.State> waiting = new ArrayList<>();





    public Composition(TransitionSystem[] systems) {
        this.systems = systems;

        clocks.addAll(Arrays.stream(systems).map(TransitionSystem::getClocks).flatMap(List::stream).collect(Collectors.toList()));

        // initialize inputs, outputs and syncs
        inputs = new HashSet<>();
        outputs = new HashSet<>();
        syncs = new HashSet<>();

        // to compute inputs, outputs and syncs of composed TS, analyse all pairs of TS's
        for (int i = 0; i < systems.length; i++) {

            // initialize inputs and outputs of TS at index i
            Set<Channel> inputsOfI = new HashSet<>(systems[i].getInputs());
            Set<Channel> outputsOfI = new HashSet<>(systems[i].getOutputs());

            // add syncs of I to global sync list
            syncs.addAll(systems[i].getSyncs());

            for (int j = 0; j < systems.length; j++) {
                if (i != j) {

                    // get inputs, outputs and syncs of TS at index j
                    Set<Channel> inputsOfJ = new HashSet<>(systems[j].getInputs());
                    Set<Channel> outputsOfJ = new HashSet<>(systems[j].getOutputs());
                    Set<Channel> syncsOfJ = new HashSet<>(systems[j].getSyncs());

                    // we need to fetch the outputs of I again, as they might have been modified in the process
                    Set<Channel> cleanOutputsOfI = new HashSet<>(systems[i].getOutputs());
                    // check if output actions overlap
                    Set<Channel> diff = setIntersection(cleanOutputsOfI, outputsOfJ);
                    if (!diff.isEmpty()) {
                        throw new IllegalArgumentException("The automata cannot be composed");
                    }

                    // we need to fetch the inputs of I again, as they might have been modified in the process
                    Set<Channel> cleanInputsOfI = new HashSet<>(systems[i].getInputs());
                    // if some inputs of one automaton overlap with the outputs of another one, add those to the global sync list
                    syncs.addAll(setIntersection(cleanInputsOfI, outputsOfJ));

                    // apply changes to inputs and outputs of TS at index i
                    inputsOfI.removeAll(outputsOfJ);
                    inputsOfI.removeAll(syncsOfJ);
                    outputsOfI.removeAll(inputsOfJ);
                    outputsOfI.removeAll(syncsOfJ);
                }
            }

            // add inputs and outputs to the global lists
            inputs.addAll(inputsOfI);
            outputs.addAll(outputsOfI);
            setMaxBounds();
        }
/*
        String combinedName = "";
        for (int i = 0; i < systems.length; i++) {
            combinedName += systems[i].getSystems().get(0).getName();
        }

        Automaton resAut = new Automaton(combinedName, new ArrayList<Location>(locationsSet), new ArrayList<Edge>(edgesSet), clocks, false);
        SimpleTransitionSystem st = new SimpleTransitionSystem(resAut);
        st.toXML("isThisAComposition"); */
    }

    public Set<Channel> getInputs() {
        return inputs;
    }

    public Set<Channel> getOutputs() {
        return outputs;
    }

    public Set<Channel> getSyncs() {
        return syncs;
    }

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<SimpleTransitionSystem> getSystems() {
        List<SimpleTransitionSystem> result = new ArrayList<>();
        for (TransitionSystem ts : systems) {
            result.addAll(ts.getSystems());
        }
        return result;
    }


    private int[] maxBounds;
    public void setMaxBounds() {
        List<Integer> res = new ArrayList<>();
        res.add(0);
        for (TransitionSystem sys : Arrays.stream(systems).collect(Collectors.toList()))
            res.addAll(sys.getMaxBounds());

        maxBounds = res.stream().mapToInt(i -> i).toArray();
    }

    public Automaton createComposition(List<Automaton> autList)
    {
        String name="";
        Set<Edge> edgesSet = new HashSet<>();
        Set<Location> locationsSet = new HashSet<>();
        Map<String, Location> locMap = new HashMap<>();
        passed = new ArrayList<>();
        waiting = new ArrayList<>();

        List<Location> initLoc = new ArrayList<>();
        for (Automaton aut : autList) {
            initLoc.add(aut.getInitLoc());
            if (name.isEmpty())
                name = aut.getName();
            else
                name += " | " + aut.getName();
        }

        Location initL = createLoc(initLoc);
        locationsSet.add(initL);

        Set<Channel> all = new HashSet<>();
        all.addAll(syncs); all.addAll(outputs); all.addAll(inputs);


        locMap.put(initL.getName(),initL);

        Refinement.State initState = getInitialState();;
        waiting.add(initState);

        while (!waiting.isEmpty())
        {
            Refinement.State currentState = (Refinement.State)waiting.toArray()[0];
            waiting.remove(currentState);
            passed.add(currentState);
            //System.out.println("Processing state " + currentState.getLocation().getName()) ;
            //if (currentState.getLocation().getName().equals("L0L5L6"))
            //    currentState.getInvFed().getZones().get(0).printDBM(true,true);

            for (Channel chan : all )
            {

                List<Refinement.Transition> transList = getNextTransitions(currentState, chan, clocks);
                for (Refinement.Transition trans : transList)
                {
                    String targetName = trans.getTarget().getLocation().getName();

                    boolean isInitial = trans.getTarget().getLocation().getIsInitial();
                    boolean isUrgent = trans.getTarget().getLocation().getIsUrgent();
                    boolean isUniversal = trans.getTarget().getLocation().getIsUniversal();
                    boolean isInconsistent = trans.getTarget().getLocation().getIsInconsistent();
                    List<List<Guard>> invariant = trans.getTarget().getInvariants();
                    String sourceName = trans.getSource().getLocation().getName();
                    int x = trans.getTarget().getLocation().getX();
                    int y = trans.getTarget().getLocation().getX();

                    Location target;
                    if (locMap.containsKey(targetName))
                        target = locMap.get(targetName);
                    else {
                        target = new Location(targetName, invariant, isInitial, isUrgent, isUniversal, isInconsistent, x, y);
                        locMap.put(targetName,target);
                    }
                    locationsSet.add(target);
                    if (!passedContains(trans.getTarget()) && !waitingContains(trans.getTarget()) ) {
                        trans.getTarget().extrapolateMaxBounds(maxBounds);
                        waiting.add(trans.getTarget());
                    }
                    List<List<Guard>> guardList = trans.getGuards(); // TODO: Check!
                    List<Update> updateList = trans.getUpdates();
                    boolean isInput = false;
                    if (inputs.contains(chan))
                        isInput= true;
                    assert(locMap.get(sourceName)!=null);
                    assert(locMap.get(targetName)!=null);

                    Edge e = new Edge(locMap.get(sourceName), locMap.get(targetName), chan, isInput, guardList, updateList.toArray(new Update[updateList.size()]));
                    boolean edgeAlreadyExists=false;
                    for (Edge otherE : edgesSet) {
                        if (otherE.getSource().equals(e.getSource()) && otherE.getTarget().equals(e.getTarget()) && otherE.getChannel().equals(e.getChannel()) && e.isInput() == otherE.isInput() && Arrays.equals(e.getUpdates(),otherE.getUpdates()))
                        {

                            if (Federation.fedEqFed(e.getGuardFederation(clocks), otherE.getGuardFederation(clocks)));
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


        Automaton resAut = new Automaton(name, new ArrayList<Location>(locationsSet), new ArrayList<Edge>(edgesSet), clocks, false);
        return resAut;

    }

    public boolean passedContains(Refinement.State s)
    {
        boolean contained = false;

        for (Refinement.State st: passed.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList()))
        {
            if (s.getInvFed().isSubset(st.getInvFed()))
                contained = true;
        }
        return contained;
    }

    public boolean waitingContains(Refinement.State s)
    {
        boolean contained = false;

        for (Refinement.State st: waiting.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList()))
        {

            if (s.getInvFed().isSubset(st.getInvFed())) {
                contained = true;
            }
        }
        return contained;
    }

    public Location createLoc(List<Location> locList)
    {
        String name="";
        List<List<Guard>> invariant = new ArrayList<>();

        List<Zone> emptyZoneList = new ArrayList<>();
        Zone emptyZone = new Zone(clocks.size() + 1, true);
        emptyZone.init();
        emptyZoneList.add(emptyZone);
        Federation invarFed = new Federation(emptyZoneList);
        boolean isInitial = true;
        boolean isUrgent = false;
        boolean isUniversal = false;
        boolean isInconsistent = false;
        int x=0, y=0;

        for (Location l : locList) {
            if (name.isEmpty())
                name = l.getName();
            else
                name += "" + l.getName();

            invarFed = l.getInvariantFederation(clocks).intersect(invarFed);
            isInitial = isInitial && l.isInitial();
            isUrgent = isUrgent || l.isUrgent();
            isUniversal = isUniversal || l.isUniversal();
            isInconsistent = isInconsistent || l.isInconsistent();
            x += l.getX();
            y += l.getY();

        }
        invariant = invarFed.turnFederationToGuards(clocks);

        return new Location(name, invariant, isInitial,isUrgent,isUniversal,isInconsistent, x/locList.size(), y / locList.size());

    }


    @Override
    public Automaton getAutomaton()
    {

        List<Automaton> autList = new ArrayList<>();
        for (int i=0; i<systems.length;i++)
            autList.add(systems[i].getAutomaton());

        Automaton resAut = createComposition(autList);
        return resAut;


    }

    // build a list of transitions from a given state and a signal
    public List<Refinement.Transition> getNextTransitions(Refinement.State currentState, Channel channel, List<Clock> allClocks) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = new ArrayList<>();

        if (checkForOutputs(channel, locations))
            resultMoves = computeResultMoves(locations, channel);
        List<Refinement.Transition> transitions = createNewTransitions(currentState, resultMoves, allClocks);
        return transitions;
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        // Check if action belongs to this TS at all before proceeding
        if (!outputs.contains(channel) && !inputs.contains(channel) && !syncs.contains(channel))
            return new ArrayList<>();

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

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        if (!moveExisted) return new ArrayList<>();
        return resultMoves;
    }

    private boolean checkForOutputs(Channel channel, List<SymbolicLocation> locations) {
        // for syncs, we must make sure we have an output first
        if (syncs.contains(channel)) {
            // loop through all automata to find the one sending the output
            for (int i = 0; i < systems.length; i++) {
                if (systems[i].getOutputs().contains(channel) || systems[i].getSyncs().contains(channel)) {
                    List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);
                    if (moves.isEmpty()) {
                        // do not check for outputs if the state in the corresponding automaton does not send that output
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Set<Channel> setIntersection(Set<Channel> set1, Set<Channel> set2) {
        Set<Channel> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return intersection;
    }
}