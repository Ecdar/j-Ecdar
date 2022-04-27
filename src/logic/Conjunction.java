package logic;

import models.*;

import java.util.*;
import java.util.stream.Collectors;

public class Conjunction extends TransitionSystem {
    private final TransitionSystem[] systems;

    private List<State> passed = new ArrayList<>();
    private List<State> waiting = new ArrayList<>();
    private HashMap<Clock,Integer> maxBounds;


    public void setMaxBounds() {
        HashMap<Clock,Integer> res = new HashMap<>();
        for (TransitionSystem sys : Arrays.stream(systems).collect(Collectors.toList()))
            res.putAll(sys.getMaxBounds());

        maxBounds = res;
    }



    public Conjunction(TransitionSystem[] systems) {
        this.systems = systems;
        setMaxBounds();
        clocks.addAll(Arrays.stream(systems).map(TransitionSystem::getClocks).flatMap(List::stream).collect(Collectors.toList()));
    }

    public Set<Channel> getInputs() {
        Set<Channel> inputs = new HashSet<>(systems[0].getInputs());

        for (int i = 1; i < systems.length; i++) {
            inputs.retainAll(systems[i].getInputs());
        }

        return inputs;
    }

    public Set<Channel> getOutputs() {
        Set<Channel> outputs = new HashSet<>(systems[0].getOutputs());

        for (int i = 1; i < systems.length; i++) {
            outputs.retainAll(systems[i].getOutputs());
        }

        return outputs;
    }

    public List<SimpleTransitionSystem> getSystems(){
        List<SimpleTransitionSystem> result = new ArrayList<>();
        for(TransitionSystem ts : systems){
            result.addAll(ts.getSystems());
        }
        return result;
    }

    public boolean passedContains(State s)
    {
        boolean contained = false;

        for (State st: passed.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList()))
        {
            if (CDD.isSubset(s.getInvarCDD(), st.getInvarCDD()))
                contained = true;
        }
        return contained;
    }

    public boolean waitingContains(State s)
    {
        boolean contained = false;

        for (State st: waiting.stream().filter(st -> st.getLocation().getName().equals(s.getLocation().getName())).collect(Collectors.toList()))
        {

            if (CDD.isSubset(s.getInvarCDD(),st.getInvarCDD())) {
                contained = true;
            }
        }
        return contained;
    }



    public Automaton createComposition(List<Automaton> autList)
    {
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(getClocks());
        CDD.addBddvar(BVs);

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
                name += " && " + aut.getName();
        }

        Location initL = createLoc(initLoc);
        locationsSet.add(initL);

        Set<Channel> all = new HashSet<>();
        all.addAll(getInputs());
        all.addAll(getOutputs());


        locMap.put(initL.getName(),initL);

        State initState = getInitialState();
        waiting.add(initState);
        while (!waiting.isEmpty())
        {

            State currentState = (State)waiting.toArray()[0];
            waiting.remove(currentState);
            passed.add(currentState);
            //System.out.println("Processing state " + currentState.getLocation().getName()) ;
            //if (currentState.getLocation().getName().equals("L0L5L6"))
            //    currentState.getInvFed().getZones().get(0).printDBM(true,true);

            for (Channel chan : all )
            {

                List<Transition> transList = getNextTransitions(currentState, chan, clocks);
                for (Transition trans : transList)
                {

                    String targetName = trans.getTarget().getLocation().getName();

                    boolean isInitial = trans.getTarget().getLocation().getIsInitial();
                    boolean isUrgent = trans.getTarget().getLocation().getIsUrgent();
                    boolean isUniversal = trans.getTarget().getLocation().getIsUniversal();
                    boolean isInconsistent = trans.getTarget().getLocation().getIsInconsistent();
                    Guard invariant = trans.getTarget().getInvariants(clocks);
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
                        trans.getTarget().extrapolateMaxBounds(maxBounds, clocks);
                        waiting.add(trans.getTarget());
                    }
                    Guard guardList = trans.getGuards(clocks); // TODO: Check!
                    List<Update> updateList = trans.getUpdates();
                    boolean isInput = false;
                    if (getInputs().contains(chan))
                        isInput= true;
                    assert(locMap.get(sourceName)!=null);
                    assert(locMap.get(targetName)!=null);

                    Edge e = new Edge(locMap.get(sourceName), locMap.get(targetName), chan, isInput, guardList, updateList);
                    boolean edgeAlreadyExists=false;
                    for (Edge otherE : edgesSet) {
                        if (otherE.getSource().equals(e.getSource()) && otherE.getTarget().equals(e.getTarget()) && otherE.getChannel().equals(e.getChannel()) && e.isInput() == otherE.isInput() && e.getUpdates().equals(otherE.getUpdates())) // TODO: fix the comparison between updates
                        {

                            if (e.getGuardCDD().equiv( otherE.getGuardCDD()));
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


        Automaton resAut = new Automaton(name, new ArrayList<Location>(locationsSet), new ArrayList<Edge>(edgesSet), clocks, BVs, false);
        CDD.done();
        return resAut;

    }



    public Location createLoc(List<Location> locList)
    {
        String name="";
        Guard invariant;

        CDD invarFed = CDD.cddTrue(); //CDD.getUnrestrainedCDD();
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

            invarFed = l.getInvariantCDD().conjunction(invarFed);
            isInitial = isInitial && l.isInitial();
            isUrgent = isUrgent || l.isUrgent();
            isUniversal = isUniversal || l.isUniversal();
            isInconsistent = isInconsistent || l.isInconsistent();
            x += l.getX();
            y += l.getY();

        }

        invariant = CDD.toGuardList(invarFed, getClocks());
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

    public SymbolicLocation getInitialLocation() {
        return getInitialLocation(systems);
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        List<SymbolicLocation> locations = ((ComplexLocation) currentState.getLocation()).getLocations();

        // these will store the locations of the target states and the corresponding transitions
        List<Move> resultMoves = computeResultMoves(locations, channel);
        if (resultMoves.isEmpty()) return new ArrayList<>();
        return createNewTransitions(currentState, resultMoves, allClocks);
    }

    public List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<SymbolicLocation> symLocs = ((ComplexLocation) symLocation).getLocations();

        return computeResultMoves(symLocs, channel);
    }

    private List<Move> computeResultMoves(List<SymbolicLocation> locations, Channel channel) {
        List<Move> resultMoves = systems[0].getNextMoves(locations.get(0), channel);
        // used when there are no moves for some TS
        if (resultMoves.isEmpty())
            return new ArrayList<>();

        for (int i = 1; i < systems.length; i++) {
            List<Move> moves = systems[i].getNextMoves(locations.get(i), channel);

            if (moves.isEmpty())
                return new ArrayList<>();

            resultMoves = moveProduct(resultMoves, moves, i == 1);
        }

        return resultMoves;
    }
}
