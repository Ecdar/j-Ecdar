package logic;

import models.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import parser.XMLFileWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleTransitionSystem extends TransitionSystem{

    private boolean printComments = false;

    private final Automaton automaton;
    private Deque<State> waiting;
    private List<State> passed;
    private List<Integer> maxBounds;

    public SimpleTransitionSystem(Automaton automaton) {
        this.automaton = automaton;
        clocks.addAll(automaton.getClocks());

        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
        setMaxBounds();
    }

    public Set<Channel> getInputs() {
        return automaton.getInputAct();
    }

    public Set<Channel> getOutputs() {
        return automaton.getOutputAct();
    }

    public SymbolicLocation getInitialLocation() {
        return new SimpleLocation(automaton.getInitLoc());
    }

    public List<SimpleTransitionSystem> getSystems() {
        return Collections.singletonList(this);
    }

    public String getName() {
        return automaton.getName();
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setMaxBounds()
    {
       // System.out.println("Max bounds: " + automaton.getMaxBoundsForAllClocks());
        List<Integer> res = new ArrayList<>();

        res.addAll(automaton.getMaxBoundsForAllClocks());
        res.replaceAll(e -> e==0 ? 1 : e);
        maxBounds = res;
    }
    public List<Integer> getMaxBounds(){
        return maxBounds;
    }

    // Checks if automaton is deterministic
    public boolean isDeterministicHelper() {


        Set<Channel> actions = getActions();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());

        while (!waiting.isEmpty()) {
            System.out.println("in the while");
            State currState = new State(waiting.pop());
            State toStore = new State(currState);
            int[] maxBounds;
            List<Integer> res = new ArrayList<>();
            res.add(0);
            res.addAll(this.getMaxBounds());
            maxBounds= res.stream().mapToInt(i -> i).toArray();

            toStore.extrapolateMaxBounds(maxBounds);
            passed.add(toStore);

            for (Channel action : actions) {

                List<Transition> tempTrans = getNextTransitions(currState, action);

                System.out.println("and now this");
                if (checkMovesOverlap(tempTrans)) {
                    System.out.println("found the culprit");
                    return false;
                }

                System.out.println("and now t´hat");

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s) && !waitingContainsState(s)).collect(Collectors.toList()); // TODO I added waitingConstainsState... Okay??

                System.out.println("and now none");
                toAdd.forEach(e->e.extrapolateMaxBounds(maxBounds));

                waiting.addAll(toAdd);
            }
        }
        System.out.println("shinfesadasdas");
        return true;
    }

    // Check if zones of moves for the same action overlap, that is if there is non-determinism
    public boolean checkMovesOverlap(List<Transition> trans) {
        if (trans.size() < 2) return false;
        //System.out.println("check moves overlap -------------------------------------------------------------------");
        for (int i = 0; i < trans.size(); i++) {
            for (int j = i + 1; j < trans.size(); j++) {
                if (trans.get(i).getTarget().getLocation().equals(trans.get(j).getTarget().getLocation())
                        && trans.get(i).getEdges().get(0).hasEqualUpdates(trans.get(j).getEdges().get(0)))
                    continue;

                State state1 = new State(trans.get(i).getSource());
                State state2 = new State(trans.get(j).getSource());

                System.out.println(trans.get(i).getEdges());
                System.out.println(trans.get(j).getEdges());
                state1.applyGuards(trans.get(i).getGuardCDD());
                state2.applyGuards(trans.get(j).getGuardCDD());



                if (state1.getInvarCDD().isNotFalse() && state2.getInvarCDD().isNotFalse()) {
                    if(CDD.intersects(state1.getInvarCDD(),state2.getInvarCDD())) {
                        //trans.get(i).getGuardCDD().printDot();
                       // trans.get(j).getGuardCDD().printDot();
                        //trans.get(i).getEdges().get(0).getGuardCDD().printDot();
                        //trans.get(j).getEdges().get(0).getGuardCDD().printDot();
                        System.out.println("they intersect??!");
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public boolean isConsistentHelper(boolean canPrune) {
        //if (!isDeterministic()) // TODO: this was commented out, I added it again
        //    return false;
        System.out.println("is determinsic check passed");
        passed = new ArrayList<>();
        boolean result = checkConsistency(getInitialState(), getInputs(), getOutputs(), canPrune);
        System.out.println("made it out of consistency check");

        return result;
    }

    public boolean checkConsistency(State currState, Set<Channel> inputs, Set<Channel> outputs, boolean canPrune) {

        if (passedContainsState(currState))
            return true;

        State toStore = new State(currState);
        int[] maxBounds;
        List<Integer> res = new ArrayList<>();
        res.add(0);
        res.addAll(this.getMaxBounds());
        maxBounds= res.stream().mapToInt(i -> i).toArray();

        toStore.extrapolateMaxBounds(maxBounds);
        passed.add(toStore);
        System.out.println("somewhere mid consistentcy check");
        // Check if the target of every outgoing input edge ensures independent progress
        for (Channel channel : inputs) {
            List<Transition> tempTrans = getNextTransitions(currState, channel);
            for (Transition ts : tempTrans) {
                boolean inputConsistent = checkConsistency(ts.getTarget(), inputs, outputs, canPrune);
                if (!inputConsistent)
                    return false;
            }
        }

        System.out.println("somewhere after mid  consistentcy check");
        boolean outputExisted = false;
        // If delaying indefinitely is possible -> Prune the rest
        if (canPrune && CDD.canDelayIndefinitely(currState.getInvarCDD())) {
            System.out.println("somewhere further");
            return true;
        }
            // Else if independent progress does not hold through delaying indefinitely,
            // we must check for being able to output and satisfy independent progress
        else {
            System.out.println("somewhere even further");
            for (Channel channel : outputs) {
                List<Transition> tempTrans = getNextTransitions(currState, channel);

                for (Transition ts : tempTrans) {
                    if(!outputExisted) outputExisted = true;
                    boolean outputConsistent = checkConsistency(ts.getTarget(), inputs, outputs, canPrune);
                    if (outputConsistent && canPrune)
                        return true;
                    if(!outputConsistent && !canPrune) {
                        System.out.println("wow");
                        return false;
                    }
                }
            }
            if(!canPrune) {
                if (outputExisted)
                    return true;
                System.out.println("finale");
                return CDD.canDelayIndefinitely(currState.getInvarCDD());

            }
            // If by now no locations reached by output edges managed to satisfy independent progress check
            // or there are no output edges from the current location -> Independent progress does not hold

            else
            {
                System.out.println("fin finale");
                return false;
            }
        }
    }

    public boolean isImplementationHelper(){
        Set<Channel> outputs = getOutputs();
        Set<Channel> actions = getActions();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());

        while (!waiting.isEmpty()) {
            State currState = new State(waiting.pop());

            State toStore = new State(currState);
            int[] maxBounds;
            List<Integer> res = new ArrayList<>();
            res.add(0);
            res.addAll(this.getMaxBounds());
            maxBounds= res.stream().mapToInt(i -> i).toArray();

            toStore.extrapolateMaxBounds(maxBounds);
            passed.add(toStore);


            for (Channel action : actions){
                List<Transition> tempTrans = getNextTransitions(currState, action);

                if(!tempTrans.isEmpty() && outputs.contains(action)){
                    if(!outputsAreUrgent(tempTrans))
                        return false;
                }

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s)).collect(Collectors.toList());

                toAdd.forEach(s -> s.extrapolateMaxBounds(maxBounds));

                waiting.addAll(toAdd);
            }
        }

        return true;
    }

    public boolean outputsAreUrgent(List<Transition> trans){
        for (Transition ts : trans){
            State state = new State(ts.getSource());
            state.applyGuards(ts.getGuardCDD());

            if(!CDD.isUrgent(state.getInvarCDD()))
                return false;
        }
        return true;
    }



    private boolean passedContainsState(State state1) {
       State state = new State(state1);
        System.out.println("start of passedstate");
        int[] maxBounds;
        List<Integer> res = new ArrayList<>();
        res.add(0);
        res.addAll(this.getMaxBounds());
        maxBounds= res.stream().mapToInt(i -> i).toArray();
        System.out.println("mid of passedstate");
        state.extrapolateMaxBounds(maxBounds);


        System.out.println("mid of passedstate");
        for (State passedState : passed) {
            if (state.getLocation().equals(passedState.getLocation()) &&
                    CDD.isSubset(state.getInvarCDD(),(passedState.getInvarCDD()))) {

                return true;
            }
        }
        System.out.println("end of passedstate");
        return false;
    }

    private boolean waitingContainsState(State state1) {
        State state = new State(state1);
        int[] maxBounds;
        List<Integer> res = new ArrayList<>();
        res.add(0);
        res.addAll(this.getMaxBounds());
        maxBounds= res.stream().mapToInt(i -> i).toArray();
        state.extrapolateMaxBounds(maxBounds);


        for (State passedState : waiting) {
            // check for zone inclusion
            if (state.getLocation().equals(passedState.getLocation()) &&
                    CDD.isSubset(state.getInvarCDD(),passedState.getInvarCDD())) {
                return true;
            }
        }

        return false;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        System.out.println("reached getNexttrans");
        List<Move> moves = getNextMoves(currentState.getLocation(), channel);
        System.out.println("made moves");
        return createNewTransitions(currentState, moves, allClocks);
    }

    protected List<Move> getNextMoves(SymbolicLocation symLocation, Channel channel) {
        List<Move> moves = new ArrayList<>();

        Location location = ((SimpleLocation) symLocation).getActualLocation();
        List<Edge> edges = automaton.getEdgesFromLocationAndSignal(location, channel);

        for (Edge edge : edges) {
            SymbolicLocation target = new SimpleLocation(edge.getTarget());
            Move move = new Move(symLocation, target, Collections.singletonList(edge));
            moves.add(move);
        }
        return moves;
    }

    public void toXML(String filename)
    {
        XMLFileWriter.toXML(filename,this);
    }


    public void toJson(String filename)
    {
        JsonFileWriter.writeToJson(this.getAutomaton(),filename);
    }


    public SimpleTransitionSystem pruneReachTimed(){
        //TODO: this function is not correct yet. // FIXED: 05.1.2021
        // In the while loop, we should collect all edges associated to transitions (not just all locations associated to states), and remove all that were never associated
        Set<Channel> outputs = getOutputs();
        Set<Channel> actions = getActions();
        // the set to store all locations we met during the exploration. All others will be removed afterwards.
        Set<Location> metLocations = new HashSet<Location>();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());

        List<Edge> passedEdges = new ArrayList<Edge>();


        // explore until waiting is empty, and add all locations that ever are in waiting to metLocations
        while (!waiting.isEmpty()) {
            State currState = new State(waiting.pop());
            passed.add(new State(currState));
            metLocations.add(((SimpleLocation) currState.getLocation()).getActualLocation());
            for (Channel action : actions){
                List<Transition> tempTrans = getNextTransitions(currState, action);
                for (Transition t: tempTrans)
                {
                    for (Edge e : t.getEdges())
                        passedEdges.add(e);
                }
                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s)).collect(Collectors.toList());
                waiting.addAll(toAdd);
            }
        }

        List<Edge> edges = new ArrayList<Edge>();
        List<Location> locations = new ArrayList<Location>();

        // we only want to add edges to our new automaton, if their target and source were to / from explored locations
        for (Edge e : getAutomaton().getEdges())
        {
            boolean sourceMatched=false;
            boolean targetMatched=false;
            for (Location l: metLocations) {
                if (e.getTarget().getName().equals(l.getName()))
                    targetMatched= true;
                if (e.getSource().getName().equals(l.getName())) {
                    sourceMatched = true;
                }

            }
            if (sourceMatched && targetMatched && passedEdges.contains(e))    edges.add(e);
        }

        // add all explored locations
        for (Location l: metLocations)
        {
            locations.add(l);
        }

        Automaton aut = new Automaton(getName(), locations, edges, getClocks(),getAutomaton().getBVs(), false);
        return new SimpleTransitionSystem(aut);
    }



    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }




}
