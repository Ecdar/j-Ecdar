package logic;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslClientHelloHandler;
import models.*;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.*;
import java.util.stream.Collectors;

public class Refinement {
    int counter =0;
    private final TransitionSystem ts1, ts2;
    private final List<Clock> allClocks;
    private final List<BoolVar> allBVs;
    private final Map<LocationPair, StatePair> passed;
    private final Map<LocationPair, StatePair> waiting;
    private final Set<Channel> inputs1, inputs2, outputs1, outputs2;
    private GraphNode refGraph;
    private GraphNode currNode;
    private GraphNode supersetNode;
    private int treeSize;
    private HashMap<Clock,Integer> maxBounds;
    private static boolean RET_REF = false;
    public static int NODE_ID = 0;
    private StringBuilder errMsg = new StringBuilder();

    public Refinement(TransitionSystem system1, TransitionSystem system2) {
        this.ts1 = system1;
        this.ts2 = system2;
        this.waiting = new HashMap<>();
        this.passed = new HashMap<>();

        allClocks = new ArrayList<>(ts1.getClocks());
        allClocks.addAll(ts2.getClocks());
        allBVs= new ArrayList<>(ts1.getBVs());
        allBVs.addAll(ts2.getBVs());


        inputs1 = ts1.getInputs();
        inputs2 = ts2.getInputs();

        outputs1 = new HashSet<>(ts1.getOutputs());
        outputs1.addAll(ts1.getSyncs());

        outputs2 = new HashSet<>(ts2.getOutputs());
        outputs2.addAll(ts2.getSyncs());

        setMaxBounds();
    }

    public String getErrMsg() {
        return errMsg.toString();
    }

    public boolean check(boolean ret_ref) { // TODO: test this.
        Refinement.NODE_ID = 0;
        Refinement.RET_REF = ret_ref;
        return checkRef();
    }

    public boolean check() {
        Refinement.RET_REF = false;
        return checkRef();
    }

    public GraphNode getTree() {
        return refGraph;
    }



    public boolean checkPreconditions() {
        boolean precondMet = true;

        // check for duplicate automata
        List<SimpleTransitionSystem> leftSystems = ts1.getSystems();
        List<SimpleTransitionSystem> rightSystems = ts2.getSystems();

        for (SimpleTransitionSystem left : leftSystems) {
            for (SimpleTransitionSystem right : rightSystems) {
                if (left.hashCode() == right.hashCode()) {
                    precondMet = false;
                    errMsg.append("Duplicate process instance: ");
                    errMsg.append(left.getName());
                    errMsg.append(".\n");
                }
            }
        }
        // signature check, precondition of refinement: inputs and outputs must be the same on both sides,
        // with the exception that the left side is allowed to have more outputs

        // inputs on the left must be equal to inputs on the right side
        if (!inputs2.containsAll(inputs1)) {
            precondMet = false;
            errMsg.append("Inputs on the left side are not subset to inputs on the right side.\n");
        }

        // the left side must contain all outputs from the right side
        if (!outputs1.containsAll(outputs2)) {
            precondMet = false;
            errMsg.append("Not all outputs of the right side are present on the left side.\n");
        }

        Set<Channel> output1Copy = new HashSet<>(outputs1);
        output1Copy.retainAll(inputs2);
        // the left side must contain all outputs from the right side
        if (!output1Copy.isEmpty()){
            precondMet = false;
            errMsg.append("Alphabet mismatch.\n");
        }

        Set<Channel> output2Copy = new HashSet<>(outputs2);
        output1Copy.retainAll(inputs1);
        // the left side must contain all outputs from the right side
        if (!output1Copy.isEmpty()){
            precondMet = false;
            errMsg.append("Alphabet mismatch.\n");
        }


        if (!ts1.isLeastConsistent()) {
            precondMet = false;
            errMsg.append(ts1.getLastErr());
        }

        if (!ts2.isLeastConsistent()) {
            precondMet = false;
            errMsg.append(ts2.getLastErr());
        }
        return precondMet;
    }

    public boolean checkRef() {
        // one or more of the preconditions failed, so fail refinement
        if (!checkPreconditions())
            return false;

        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(allClocks);
        CDD.addBddvar(allBVs);

        // the first states we look at are the initial ones
        StatePair initPair = getInitialStatePair();
        LocationPair initLocPair = new LocationPair(initPair.getLeft().getLocation(),initPair.getRight().getLocation());
        waiting.put(initLocPair, initPair);

        if (RET_REF) {
            refGraph = new GraphNode(initPair);
            currNode = refGraph;
            treeSize++;
        }


        while (!waiting.isEmpty()) {
            Map.Entry entry = (Map.Entry) waiting.entrySet().toArray()[0];
            LocationPair locPairCurr = (LocationPair) entry.getKey();
            StatePair curr = (StatePair) entry.getValue();

            waiting.remove(locPairCurr);

            if (RET_REF)
                currNode = curr.getNode();

            State left = curr.getLeft();
            State right = curr.getRight();
            // need to make deep copy
            State newState1 = new State(left);
            State newState2 = new State(right);
            // mark the pair of states as visited
            LocationPair locPair = new LocationPair(left.getLocation(), right.getLocation());
            StatePair pair = new StatePair(newState1, newState2, currNode);

           /* if (curr.getLeft().getLocation().getName().contains("L5L9"))
                if (curr.getRight().getLocation().getName().equals("L18DIVL1")) {
                    System.out.println("Current state: " + curr.prettyPrint());
                    System.out.println(waiting.size() + " " + passed.size());
                }*/
            //assert(!waitingContainsStatePair(curr));
            //assert(!passedContainsStatePair(curr));





            if (!passed.containsKey(locPair)) {
                for (LocationPair keyPair : passed.keySet())
                   if (keyPair.equals(locPair)) {
                       System.out.println("rest");
                       assert (false);
                   }
            }




            if (passed.containsKey(locPair)) {
                passed.get(locPair).getLeft().disjunctCDD(left.getInvarCDD());
                passed.get(locPair).getRight().disjunctCDD(right.getInvarCDD());
            }
            else
                passed.put(locPair,pair);

           // assert(passedContainsStatePair(curr));
            // check that for every output in TS 1 there is a corresponding output in TS 2
            boolean holds1 = checkOutputs(left, right);
            if (!holds1) {

                System.out.println("not holds 1");
                CDD.done();
                return false;
            }
            //System.out.println("done with first check");
            // check that for every input in TS 2 there is a corresponding input in TS 1
            boolean holds2 = checkInputs(left, right);
            if (!holds2) {

                System.out.println("not holds 2");
                CDD.done();
                return false;
            }
        }

        // if we got here it means refinement property holds
        CDD.done();
        return true;
    }

    public int getHashMapTotalSize(Map<LocationPair, List<StatePair>> map){
        int result = 0;

        for (Map.Entry<LocationPair, List<StatePair>> entry : map.entrySet()) {
            result += entry.getValue().size();
        }

        return result;
    }

    private StatePair buildStatePair(Transition t1, Transition t2) {
        State target1 = new State(t1.getTarget().getLocation(), t1.getGuardCDD());

        target1.applyGuards(t2.getGuardCDD());
        if (target1.getInvarCDD().isFalse()) {
            return null;
        }

        target1.applyResets(t1.getUpdates());
        target1.applyResets(t2.getUpdates());

        target1.delay();

        target1.applyInvariants(t1.getTarget().getInvarCDDDirectlyFromInvariants());

        CDD invariantTest = new CDD(target1.getInvarCDD().getPointer());
        target1.applyInvariants(t2.getTarget().getInvarCDDDirectlyFromInvariants());

        // Check if the invariant of the other side does not cut solutions and if so, report failure
        // This also happens to be a delay check

        CDD cdd = invariantTest.minus(target1.getInvarCDD()).removeNegative().reduce();
        if (cdd.isNotFalse()){
            return null;
         }

        // This line can never be triggered, because the transition will not even get constructed if the invariant breaks it
        // The exact same check will catch it but in TransitionSystem instead
        //if (!target1.getInvZone().isValid()) return null;
        //if ( target1.getInvarCDD().equiv(CDD.getUnrestrainedCDD()))
        //    assert(false);
        boolean print = false;
        counter ++;
        if (counter == 507)
            System.out.println("QUOTIENT SIDE: " + t2.getTarget().getLocation().getName() + " OTHER: " + target1.getLocation().getName());

        /*for (Clock c: allClocks)
            //if (c.getName().equals("quo_new"))
            {
                maxBounds.remove(c);
                maxBounds.put(c,20);
            }*/

        target1.extrapolateMaxBounds(maxBounds,allClocks);

       // if ( target1.getInvarCDD().equiv(CDD.getUnrestrainedCDD()))
       //     assert(false);
        State target2 = new State(t2.getTarget().getLocation(), target1.getInvarCDD());
        return new StatePair(target1, target2);
    }

    private boolean createNewStatePairs(List<Transition> trans1, List<Transition> trans2) {
        boolean pairFound = false;


        List<CDD> gzLeft = trans1.stream().map(Transition::getGuardCDD).collect(Collectors.toList());
        List<CDD> gzRight = trans2.stream().map(Transition::getGuardCDD).collect(Collectors.toList());

        CDD leftCDD = CDD.cddFalse();
        for (CDD c: gzLeft)
            leftCDD = leftCDD.disjunction(c);

        CDD rightCDD = CDD.cddFalse();
        for (CDD c: gzRight)
            rightCDD = rightCDD.disjunction(c);

        //System.out.println("create new state pair");

       // leftCDD.minus(rightCDD).printDot();
       // leftCDD.minus(rightCDD).reduce().removeNegative().printDot();
        // If trans2 does not satisfy all solution of trans2, return empty list which should result in refinement failure
        if (leftCDD.minus(rightCDD).isNotFalse()) {
            System.out.println("trans 2 does not satisfiy all solutions " + trans2.get(0).getEdges().get(0).getChan());
            System.out.println(leftCDD);
            System.out.println(rightCDD);
            System.out.println(leftCDD.minus(rightCDD));
            return false;
        }
        for (Transition transition1 : trans1) {
            for (Transition transition2 : trans2) {
                StatePair pair = buildStatePair(transition1, transition2);
                if (pair != null) {
                    pairFound = true;
                    if (!waitingContainsStatePair(pair) && !passedContainsStatePair(pair)  ) {
                        LocationPair locPair = new LocationPair(pair.getLeft().getLocation(),pair.getRight().getLocation());
                        if (waiting.containsKey(locPair)) {
                            waiting.get(locPair).getLeft().disjunctCDD(pair.getLeft().getInvarCDD());
                            waiting.get(locPair).getRight().disjunctCDD(pair.getRight().getInvarCDD());
                        }
                        else
                            waiting.put(locPair,pair);

                        if (RET_REF) {
                            currNode.constructSuccessor(pair, transition1.getEdges(), transition2.getEdges());
                            treeSize++;
                        }
                    } else {
                        if (RET_REF && supersetNode != null && !currNode.equals(supersetNode)) {
                            GraphEdge edge = new GraphEdge(currNode, supersetNode, transition1.getEdges(), transition2.getEdges(), (pair.getLeft().getInvarCDD()));
                            currNode.addSuccessor(edge);
                            supersetNode.addPredecessor(edge);
                        }
                    }
                }
            }
        }
        return pairFound;
    }

    private boolean checkInputs(State state1, State state2) {
        return checkActions(state1, state2, true);
    }

    private boolean checkOutputs(State state1, State state2) {
        return checkActions(state1, state2, false);
    }

    private boolean checkActions(State state1, State state2, boolean isInput) {
        for (Channel action : (isInput ? inputs2 : outputs1)) {
            List<Transition> leaderTransitions = isInput ? ts2.getNextTransitions(state2, action, allClocks)
                    : ts1.getNextTransitions(state1, action, allClocks);

            if (!leaderTransitions.isEmpty()) {

                List<Transition> followerTransitions;
                Set<Channel> toCheck = isInput ? inputs1 : outputs2;
                if (toCheck.contains(action)) {
                    followerTransitions = isInput ? ts1.getNextTransitions(state1, action, allClocks)
                            : ts2.getNextTransitions(state2, action, allClocks);

                    if (followerTransitions.isEmpty()) {
                        state2.getInvarCDD().printDot();
                        System.out.println("followerTransitions empty");
                        return false;
                    }
                } else {
                    // if action is missing in TS1 (for inputs) or in TS2 (for outputs), add a self loop for that action
                    followerTransitions = new ArrayList<>();
                    if (isInput) {
                        Transition loop = new Transition(state1, state1.getInvarCDD());
                        followerTransitions.add(loop);
                    }
                    else {
                        Transition loop = new Transition(state2, state2.getInvarCDD());
                        followerTransitions.add(loop);
                    }

                }


                if(!(isInput ? createNewStatePairs(followerTransitions, leaderTransitions) : createNewStatePairs(leaderTransitions, followerTransitions))) {
                    System.out.println(isInput);
                    System.out.println("followerTransitions: " + followerTransitions.size());
                    for (Transition t: followerTransitions)
                        for (Edge e : t.getEdges())
                            System.out.println(e);
                    System.out.println("leaderTransitions: " + leaderTransitions.size());
                    for (Transition t: leaderTransitions)
                        for (Edge e : t.getEdges())
                            System.out.println(e);
                    System.out.println("create pairs failed");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean passedContainsStatePair(StatePair pair) {
       LocationPair locPair = new LocationPair(pair.getLeft().getLocation(), pair.getRight().getLocation());

        if (passed.containsKey(locPair)) {
            boolean res = listContainsStatePair(pair, passed);
            return res ;
        }
        return false;
    }

    private boolean waitingContainsStatePair(StatePair pair) {

        LocationPair locPair = new LocationPair(pair.getLeft().getLocation(), pair.getRight().getLocation());
        if (waiting.containsKey(locPair)) {
            boolean res = listContainsStatePair(pair, waiting);
            return res ;
        }
        return false;
    }

    private boolean listContainsStatePair(StatePair pair, Map<LocationPair,StatePair> pairs) {
        State currLeft = pair.getLeft();
        State currRight = pair.getRight();

        StatePair state = pairs.get(new LocationPair(pair.getLeft().getLocation(),pair.getRight().getLocation()));
        State passedLeft = state.getLeft();
        State passedRight = state.getRight();
        if (CDD.isSubset(currLeft.getInvarCDD(),passedLeft.getInvarCDD()) &&
                CDD.isSubset(currRight.getInvarCDD(),passedRight.getInvarCDD())) {
            supersetNode = state.getNode();
            return true;
        }

        return false;
    }

    public StatePair getInitialStatePair() {
        State left = ts1.getInitialStateRef( ts2.getInitialLocation().getInvariantCDD());
        State right = ts2.getInitialStateRef(ts1.getInitialLocation().getInvariantCDD());
        return new StatePair(left, right);
    }

    public void setMaxBounds() {
        HashMap<Clock,Integer> res = new HashMap<>();
        res.putAll(ts1.getMaxBounds());
        res.putAll(ts2.getMaxBounds());
        System.out.println("BOUNDS: " + res);
        maxBounds = res;
    }






}
