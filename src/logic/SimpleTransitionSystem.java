package logic;

import com.sun.deploy.security.SelectableSecurityManager;
import lib.DBMLib;
import models.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.stream.Collectors;

public class SimpleTransitionSystem extends TransitionSystem{

    private boolean printComments = false;

    private final Automaton automaton;
    private Deque<State> waiting;
    private List<State> passed;

    public SimpleTransitionSystem(Automaton automaton) {
        this.automaton = automaton;
        clocks.addAll(automaton.getClocks());

        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
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

    public List<Integer> getMaxBounds(){
        return automaton.getMaxBoundsForAllClocks();
    }

    // Checks if automaton is deterministic
    public boolean isDeterministicHelper() {

        //System.out.println("reached isdetermHelp1");
        Set<Channel> actions = getActions();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());
        //System.out.println("init state added " + getInitialState().getLocation());
        //getInitialState().getInvFed().getZones().get(0).printDBM(true,true);
        while (!waiting.isEmpty()) {
            State currState = new State(waiting.pop());
            passed.add(new State(currState));

            for (Channel action : actions) {

                List<Transition> tempTrans = getNextTransitions(currState, action);
                // System.out.println("reached isdetermHelp6");
                if (checkMovesOverlap(tempTrans)) {

                    //System.out.println("reached isdetermHelp3");
                    return false;
                }
//                System.out.println("reached isdetermHelp2");

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s) && !waitingContainsState(s)).collect(Collectors.toList()); // TODO I added waitingConstainsState... Okay??

                //              System.out.println("reached isdetermHelp5 " + toAdd.size());
                waiting.addAll(toAdd);
            }
        }

        // System.out.println("reached isdetermHelp4");
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




                state1.applyGuards(trans.get(i).getGuards(), clocks);
                state2.applyGuards(trans.get(j).getGuards(), clocks);

                if (state1.getInvFed().isValid() && state2.getInvFed().isValid()) {
                    if(state1.getInvFed().intersects(state2.getInvFed())) {


                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isConsistentHelper(boolean canPrune) {
        //if (!isDeterministic())
        //    return false;
        passed = new ArrayList<>();
        return checkConsistency(getInitialState(), getInputs(), getOutputs(), canPrune);
    }

    public boolean checkConsistency(State currState, Set<Channel> inputs, Set<Channel> outputs, boolean canPrune) {

        if (passedContainsState(currState))
            return true;

        passed.add(new State(currState));

        // Check if the target of every outgoing input edge ensures independent progress
        for (Channel channel : inputs) {
            List<Transition> tempTrans = getNextTransitions(currState, channel);
            for (Transition ts : tempTrans) {
                boolean inputConsistent = checkConsistency(ts.getTarget(), inputs, outputs, canPrune);
                if (!inputConsistent)
                    return false;
            }
        }

        boolean outputExisted = false;
        // If delaying indefinitely is possible -> Prune the rest
        if (canPrune && currState.getInvFed().canDelayIndefinitely())
            return true;
            // Else if independent progress does not hold through delaying indefinitely,
            // we must check for being able to output and satisfy independent progress
        else {
            for (Channel channel : outputs) {
                List<Transition> tempTrans = getNextTransitions(currState, channel);

                for (Transition ts : tempTrans) {
                    if(!outputExisted) outputExisted = true;
                    boolean outputConsistent = checkConsistency(ts.getTarget(), inputs, outputs, canPrune);
                    if (outputConsistent && canPrune)
                        return true;
                    if(!outputConsistent && !canPrune)
                        return false;
                }
            }
            if(!canPrune) {
                if (outputExisted)
                    return true;
                return currState.getInvFed().canDelayIndefinitely();

            }
            // If by now no locations reached by output edges managed to satisfy independent progress check
            // or there are no output edges from the current location -> Independent progress does not hold
            else return false;
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
            passed.add(new State(currState));

            for (Channel action : actions){
                List<Transition> tempTrans = getNextTransitions(currState, action);

                if(!tempTrans.isEmpty() && outputs.contains(action)){
                    if(!outputsAreUrgent(tempTrans))
                        return false;
                }

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s)).collect(Collectors.toList());

                waiting.addAll(toAdd);
            }
        }

        return true;
    }

    public boolean outputsAreUrgent(List<Transition> trans){
        for (Transition ts : trans){
            State state = new State(ts.getSource());
            state.applyGuards(ts.getGuards(), clocks);

            if(!state.getInvFed().isUrgent())
                return false;
        }
        return true;
    }
    private int counter=0;
    private boolean passedContainsState(State state) {

        //System.out.println("Checking state for passed " + state.getLocation());

        //state.getInvFed().getZones().get(0).printDBM(true,true);


        //System.out.println("*************************** in ");
        for (State passedState : passed) {
            if ((state.getLocation().equals(passedState.getLocation()))) {
                //passedState.getInvFed().getZones().get(0).printDBM(true, true);
                // check for zone inclusion
                // System.out.println(passedState.getLocation());
            }

            if (state.getLocation().equals(passedState.getLocation()) &&
                    state.getInvFed().isSubset(passedState.getInvFed())) {

                //      System.out.println("************************************ reached passedContState 3");
                return true;
            }
        }
        //System.out.println("***************************** out");




        //      System.out.println("reached passedContState 2");
        assert(counter <=100);
        return false;
    }

    private boolean waitingContainsState(State state) {

        for (State passedState : waiting) {
            //if ((state.getLocation().equals(passedState.getLocation())))
            //passedState.getInvFed().getZones().get(0).printDBM(true,true);
            // check for zone inclusion
            if (state.getLocation().equals(passedState.getLocation()) &&
                    state.getInvFed().isSubset(passedState.getInvFed())) {
                // System.out.println("*************************** reached passedContState 3");
                return true;
            }
        }

        return false;
    }

    public List<Transition> getNextTransitions(State currentState, Channel channel, List<Clock> allClocks) {
        //System.out.println("reached getNexttrans");
        List<Move> moves = getNextMoves(currentState.getLocation(), channel);

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
        // System.out.println("reached getnextmoves  " +symLocation + channel + moves.size());
        return moves;
    }

    public void toXML(String filename)
    {
        //String file =" <?xml version="1.0" encoding="utf-8"?><!DOCTYPE nta PUBLIC '-//Uppaal Team//DTD Flat System 1.1//EN' 'http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd'><nta><declaration>// Place global declarations here."

        Element nta=new Element("nta");
        Document doc=new Document();
        Element declaration = new Element("declaration");
        nta.addContent(declaration);

        String decString = "";
        for (Channel c : getInputs())
            decString+= "chan " + c.getName() + "; ";
        for (Channel c : getOutputs())
            decString+= "chan " + c.getName() + "; ";


        declaration.addContent(decString);
        Element aut = new Element("template");
        Element name = new Element("name");
        name.addContent(getName());
        aut.addContent(name);


        Element localDeclaration = new Element("declaration");
        aut.addContent(localDeclaration);
        String localDecString="";
        for (Clock c : getClocks())
        {
            localDecString+= "clock " + c.getName() + "; ";
        }
        localDeclaration.addContent(localDecString);

        for (Location l : automaton.getLocations())
        {
            Element loc = new Element("location");
            Element locname = new Element("name");
            locname.addContent(l.getName());
            loc.addContent(locname);
            loc.setAttribute("id",l.getName());

            Element invarLabel = new Element("label");
            invarLabel.setAttribute("kind", "invariant");
            String guardString ="";
            int j=0;
            for (List<Guard> list: l.getInvariant()) {
                int i= 0;
                String inner="";
                for (Guard g : list) {
                    //System.out.println(g);
                    String interm = "";
                    String lower = "";
                    String upper = "";
                    if (g.isStrict()) {
                        lower = g.getClock().getName() + ">" + g.getLowerBound();
                        upper = g.getClock().getName() + "<" + g.getUpperBound();
                    } else {
                        lower = g.getClock().getName() + ">=" + g.getLowerBound();
                        upper = g.getClock().getName() + "<=" + g.getUpperBound();
                    }

                    if (g.getLowerBound() != 0)
                        if (interm.isEmpty())
                            interm += lower;
                        else
                            interm += " && " + lower;
                    if (g.getUpperBound() != 2147483647)
                        if (interm.isEmpty())
                            interm += upper;
                        else
                            interm += " && " + upper;

                    if (i == 0)
                        inner += interm;
                    else
                        inner += " && " + interm;
                    if (!inner.isEmpty()) i++;
                }
                if (j == 0)
                    guardString += inner;
                else
                    guardString += " || " + inner;
                if (!guardString.isEmpty()) j++;
            }
            invarLabel.addContent(guardString);
            if (l.isInconsistent())
                loc.setAttribute("color","#A66C0F");
            loc.addContent(invarLabel);




            aut.addContent(loc);
        }
        Element init = new Element("init");
        init.setAttribute("ref",automaton.getInitLoc().getName());
        aut.addContent(init);
        for (Edge e : automaton.getEdges())
        {
            Element edge = new Element("transition");
            if (getInputs().contains(e.getChannel()))
                edge.setAttribute("controllable","true");
            else
                edge.setAttribute("controllable","false");

            Element source = new Element("source");
            source.setAttribute("ref", e.getSource().getName());
            edge.addContent(source);
            Element target = new Element("target");
            target.setAttribute("ref", e.getTarget().getName());
            edge.addContent(target);

            Element synchlabel = new Element("label");
            synchlabel.setAttribute("kind", "synchronisation");
            if (getInputs().contains(e.getChannel()))
                synchlabel.addContent(e.getChannel().getName()+"?");
            else
                synchlabel.addContent(e.getChannel().getName()+"!");
            edge.addContent(synchlabel);

            Element guardlabel = new Element("label");
            guardlabel.setAttribute("kind", "guard");
            String guardString ="";
            int i= 0; int j=0;
            for (List<Guard> disjunction: e.getGuards())
            {
                if (j!=0)
                    guardString=guardString +" or ";
                i=0;
                for (Guard g: disjunction)
                {
                    //System.out.println(g);
                    String interm = "";
                    String lower ="";
                    String upper="";
                    if (g.isStrict()) {
                        lower = g.getClock().getName() + ">" + g.getLowerBound();
                        upper = g.getClock().getName() + "<" + g.getUpperBound();
                    }
                    else
                    {
                        lower = g.getClock().getName() + ">=" + g.getLowerBound();
                        upper = g.getClock().getName() + "<=" + g.getUpperBound();
                    }

                    if (g.getLowerBound()!=0)
                        if (interm.isEmpty())
                            interm += lower;
                        else
                            interm += " && " +lower;
                    if (g.getUpperBound()!= 2147483647)
                        if (interm.isEmpty())
                            interm += upper;
                        else
                            interm += " && " +upper;

                    if (i==0)
                        guardString+= interm;
                    else
                        guardString += " && " + interm;
                    if (!guardString.isEmpty()) i++;
                }
                j++;
            }
            guardlabel.addContent(guardString);
            edge.addContent(guardlabel);

            Element updatelabel = new Element("label");
            updatelabel.setAttribute("kind", "assignment");
            String updateString = "";
            i= 0;
            for (Update u: e.getUpdates())
            {

                if (i==0) {
                    updateString += u.getClock().getName();
                    updateString += " = " + u.getValue();
                }
                else
                    updateString += ", " + u.getClock().getName() + " = " + u.getValue();
                i++;
            }
            updatelabel.addContent(updateString);
            edge.addContent(updatelabel);
            aut.addContent(edge);
        }
        nta.addContent(aut);
        Element sys = new Element("system");
        sys.addContent("system "+ getName() +";");
        nta.addContent(sys);
        doc.setRootElement(nta);
        XMLOutputter outter = new XMLOutputter();
        outter.setFormat(Format.getPrettyFormat());
        try {
            outter.output(doc, new FileWriter(new File(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        automaton.

    }

    // this is untimed
    @Deprecated
    public SimpleTransitionSystem pruneInc() {


        Location inc = getAutomaton().getLocations().stream().filter(l -> l.getName().equals("inc")).collect(Collectors.toList()).get(0);
        List<Location> toDelete = new ArrayList<Location>();
        List<Location> toExplore = new ArrayList<Location>();
        toExplore.add(inc);

        while (!toExplore.isEmpty()) {
            Location current = toExplore.get(0);
            toExplore.remove(current);
            toDelete.add(current);
            for (Edge e : getAutomaton().getEdges()) {
                if (e.getTarget().getName().equals(current.getName()) && e.isInput()) {
                    if (!toExplore.contains(e.getSource()) && !toDelete.contains(e.getSource()))
                        toExplore.add(e.getSource());

                }
            }
        }


        List<Edge> edges = getAutomaton().getEdges();
        List<Location> locations = getAutomaton().getLocations();
        List<Edge> edgesPruned = new ArrayList<Edge>();
        List<Location> locationsPruned = new ArrayList<Location>();

        for (Edge e : edges)
        {
            if (!toDelete.contains(e.getTarget()) && !toDelete.contains(e.getSource()))
                edgesPruned.add(e);
        }

        for (Location l: locations)
        {
            if (!toDelete.contains(l))
                locationsPruned.add(l);
        }
        Automaton aut = new Automaton(getName(), locationsPruned, edgesPruned, getClocks(), false);
        SimpleTransitionSystem simp = new SimpleTransitionSystem(aut);
        return simp;

    }

    // untimed, do not use
    @Deprecated
    public SimpleTransitionSystem pruneReach()
    {
        List<Location> explored = new ArrayList<Location>();
        List<Location> toExplore = new ArrayList<Location>();
        toExplore.add(getAutomaton().getInitLoc());


        while (!toExplore.isEmpty())
        {
            Location current = toExplore.get(0);
            toExplore.remove(current);
            explored.add(current);
            for (Edge e: getAutomaton().getEdges())
            {
                if (e.getSource().getName().equals( current.getName()))
                {
                    if (!toExplore.contains(e.getTarget()) && !explored.contains(e.getTarget())) toExplore.add(e.getTarget());

                }
            }
        }

        List<Edge> edges = getAutomaton().getEdges();
        List<Location> locations = getAutomaton().getLocations();
        List<Edge> edgesPruned = new ArrayList<Edge>();
        List<Location> locationsPruned = new ArrayList<Location>();

        for (Edge e : edges)
        {
            if (explored.contains(e.getTarget()) && explored.contains(e.getSource()))
                edgesPruned.add(e);
        }

        for (Location l: locations)
        {
            if (explored.contains(l))
                locationsPruned.add(l);
        }


        edges.removeIf( e -> !explored.contains(e.getTarget()));
        edges.removeIf( e -> !explored.contains(e.getSource()));
        locations.removeIf(l -> !explored.contains(l));


        Automaton aut = new Automaton(getName(), locationsPruned, edgesPruned, getClocks(), false);
        SimpleTransitionSystem simp = new SimpleTransitionSystem(aut);
        return simp;

    }


    public SimpleTransitionSystem pruneReachTimed(){
        //TODO: this function is not correct yet.
        // In the while loop, we should collect all edges associated to transitions (not just all locations associated to states), and remove all that were never associated
        Set<Channel> outputs = getOutputs();
        Set<Channel> actions = getActions();
        // the set to store all locations we met during the exploration. All others will be removed afterwards.
        Set<Location> metLocations = new HashSet<Location>();

        waiting = new ArrayDeque<>();
        passed = new ArrayList<>();
        waiting.add(getInitialState());


        // explore until waiting is empty, and add all locations that ever are in waiting to metLocations
        while (!waiting.isEmpty()) {
            State currState = new State(waiting.pop());
            passed.add(new State(currState));
            metLocations.add(((SimpleLocation) currState.getLocation()).getActualLocation());
            for (Channel action : actions){
                List<Transition> tempTrans = getNextTransitions(currState, action);
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
                if (e.getTarget().getName().equals(l.getName()))  // TODO: Figure out why I had to match by name, instead of included in metLocations
                    targetMatched= true;
                if (e.getSource().getName().equals(l.getName())) {
                    sourceMatched = true;
                }

            }
            if (sourceMatched && targetMatched)    edges.add(e);
        }

        // add all explored locations
        for (Location l: metLocations)
        {
            locations.add(l);
        }

        Automaton aut = new Automaton(getName(), locations, edges, getClocks(), false);
        return new SimpleTransitionSystem(aut);
    }



    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }

    public static List<List<Guard>> cartesianProduct(List<List<Guard>> lists) {

        List<List<Guard>> product = new ArrayList<List<Guard>>();

        for (List<Guard> list : lists) {

            List<List<Guard>> newProduct = new ArrayList<List<Guard>>();

            for (Guard listElement : list) {

                if (product.isEmpty()) {

                    List<Guard> newProductList = new ArrayList<Guard>();

                    newProductList.add(listElement);
                    newProduct.add(newProductList);
                } else {

                    for (List<Guard> productList : product) {

                        List<Guard> newProductList = new ArrayList<Guard>(productList);
                        newProductList.add(listElement);
                        newProduct.add(newProductList);
                    }
                }
            }

            product = newProduct;
        }

        return product;
    }

    public static List<List<Guard>> cartesianProductBig(List<List<List<Guard>>> lists) {

        List<List<Guard>> product = new ArrayList<List<Guard>>();

        for (List<List<Guard>> list : lists) {

            List<List<Guard>> newProduct = new ArrayList<List<Guard>>();

            for (List<Guard> listElement : list) {

                if (product.isEmpty()) {

                    List<List<Guard>> newProductList = new ArrayList<List<Guard>>();
                    newProductList.add(listElement);
                    newProduct.addAll(newProductList);
                } else {

                    for (List<Guard> productList : product) {

                        List<Guard> newProductList = new ArrayList<Guard>(productList);
                        newProductList.addAll(listElement);
                        newProduct.add(newProductList);
                    }
                }
            }

            product = newProduct;
        }

        return product;
    }



    public static List<List<Guard>> negateGuards(List<List<Guard>> origGuards)
    {
//        neg ((a && b) || (c && d) || (e && f && g))

//        neg (a && b) && neg (c && d) && neg (e && f && g)

//        (neg a || neg b) && (neg c || neg d) && (neg e || neg f || neg g)

//        (neg a && neg c && neg e) || (neg a && neg c && neg f) || (neg a && neg c && neg g) || ....


        List<List<Guard>> bigListOfNegatedConstraints = new ArrayList<>();
        for (List<Guard> disj : origGuards)
        {
            List <Guard> temp = new ArrayList<>();
            for (Guard g : disj) {
                temp.add(g.negate());
            }
            bigListOfNegatedConstraints.add(temp);
        }

        return (cartesianProduct(bigListOfNegatedConstraints));

    }





    public SimpleTransitionSystem pruneIncTimed(){
        if (printComments)
            System.out.println(getClocks());

        List<Edge> edges = new ArrayList<Edge>();
        List<Location> locations = new ArrayList<Location>();

        Map<Location,Location> locMap = new HashMap<>();

        // Creating the sets of locations and transitions that will form the new automaton.
        // NOTE: to make this algorithm smoother, i removed the "final" from the private variables in transitions and locations, and created getters and setters.
        for (Location l : getAutomaton().getLocations()) {
            Location lNew = new Location(l.getName(), l.getInvariant(), l.isInitial(), l.isUrgent(), l.isUniversal(), l.isInconsistent());
            locations.add(lNew);
            locMap.put(l, lNew);
        }
        for (Edge e: getAutomaton().getEdges()) {
            // TODO: Make sure that guards and such things are deepcopied
            // TODO: Move this to a seperate function for deepcopying an automaton
            edges.add(new Edge(locMap.get(e.getSource()), locMap.get(e.getTarget()), e.getChannel(), e.isInput(), e.getGuards(), e.getUpdates()));
        }

        // Create a list of inconsistent locations, that we can loop through
        Set<Location> inc = new HashSet<>(locations.stream().filter(l -> l.isInconsistent()).collect(Collectors.toList()));

        // TODO: make sure the passedPairs check is actually working
        Map<Location, Federation> passedPairs = new HashMap<Location, Federation>();
        for (Location l : inc)
        {
            passedPairs.put(l,null);
        }

        Iterator<Location> iter = inc.iterator();
        // as long as there is an inconsistent location we have not processed yet
        while (iter.hasNext()) {
            // select the first inconsistent location in the set.
            Location finalInc = iter.next(); // TODO: speed optimization: start with the ones that are fully inconsistent
            if (printComments)
                System.out.println("Handling the location " + finalInc);

            // for all transitions to inc that are not selfloops // TODO: I just changes this to include selfloops. Make sure this still works!
            for (Edge e : edges.stream().filter(e -> e.getTarget().equals(finalInc)).collect(Collectors.toList())){ // && !e.getSource().equals(finalInc)).collect(Collectors.toList())) {
                if (printComments)
                    System.out.println("New Edge");
                if (!e.isInput()) {
                    if (printComments)
                        System.out.println("Handling an output to inc.");
                    // For an output, we have two cases.
                    // a) If the inconsistentPart of the target location was not defined, i.e., the whole location is inconsistent, we just remove the transition
                    // b) if the target location has an inconsistent part, we first free the clocks reset by the current transition in that federation,
                    //    then we strengthen the guard, so that it can never reach that part.
                    //List<Guard> temp = new ArrayList<Guard>();
                    //temp.addAll(e.getGuards());
                    if (finalInc.getInconsistentPart()== null) { // a)
                        edges.remove(e);
                    }
                    else //b)
                    {
                        // I could apply the target and source invariant here.
                        // However, since the main point of this is not to check whether the inconsistent part is reachable,
                        // but simply to modify the guard so that it is not, this should be sufficient.
                        // It might just add the target invariant to the guard.

                        // take the inconsistent federation and free the clocks of the output transition
                        Federation fedAfterReset = new Federation(finalInc.getInconsistentPart().getZones());
                        for (Update u: e.getUpdates())
                            fedAfterReset.free(getIndexOfClock(u.getClock(),clocks));

                        // turn the federation into guards, and add their negation to the transition.

                        List<List<List<Guard>>> big = new ArrayList<>();
                        big.add(e.getGuards());
                        List<List<Guard>> originGuardsOfFed = new ArrayList<List<Guard>>();
                        for (Zone z : fedAfterReset.getZones()) {
                            List<Guard> zoneConstraints = z.buildGuardsFromZone(clocks);
                            originGuardsOfFed.add(zoneConstraints);
                        }
                        List<List<Guard>> negationOfFedGuards = negateGuards(originGuardsOfFed);
                        big.add(negationOfFedGuards);
                        e.setGuards(cartesianProductBig(big));




/*
                        // the inconsistent part is a Federation, that is, a disjunction of Zones.
                        // Thus, when we check the guard, only one needs to be enabled,
                        // but if we apply the reset, that needs to be done to every single Zone.
                        // TODO: I assume that I can remove every Zone that is not satisfiable anymore. (Keeping track that if I remove the last one, that makes the fed unresolvable.
                        // TODO: I assume we do not need past(inc). ===> might not be true, for inconsistent parts that were taken over a transition via a previous step.

                        Federation incFed = new Federation(finalInc.getInconsistentPart().getZones());
                        // 1st step: past of every zone. TODO: what is the difference between down and free down? Am I using the wrong one?
                        for (Zone z : incFed.getZones())
                        {
                            for (Clock c : clocks)
                                DBMLib.dbm_freeDown(z.getDbm(),clocks.size()+1, getIndexOfClock(c,clocks));
                        }

                        // 2nd step: apply clock resets as guards. Only continue with zones that are satisfiable.
                        List<Zone> satisfiableZones = new ArrayList<Zone>();
                        boolean oneZoneIsSatisfiable = false;
                        for (Zone z : incFed.getZones())
                        {
                            for (Update u: e.getUpdates()) {
                                z.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), true), getIndexOfClock(u.getClock(),clocks) );
                            }
                            if (DBMLib.dbm_isEmpty(z.getDbm(),clocks.size()+1))
                            {
                                // clock resets by e would violate this zone, so we can remove it, i.e. not add it to the list of zones we take over
                                // This means that when resetting this clock, we cannot reach the inconsistent part anymore
                            }
                            else
                            {
                                oneZoneIsSatisfiable= true;
                                satisfiableZones.add(z);
                            }
                        }
                        // if no zone is satisfiable, the transition cannot reach the inc. part, so we can leave it be, else go to step 3:
                        if (oneZoneIsSatisfiable) {
                            // step 3: apply the updates, i.e., change them into guards, so that you can restrict them.
                            Federation fedAfterReset = new Federation(satisfiableZones);
                            List<Zone> restZones = new ArrayList<Zone>();
                            for (Zone z : fedAfterReset.getZones())
                            {
                                for (Update u: e.getUpdates()) {
                                    restZones.add(new Zone(DBMLib.dbm_freeClock(z.getDbm(), clocks.size() + 1, getIndexOfClock(u.getClock(),clocks))));
                                }
                                if (e.getUpdates().length==0)
                                    restZones.add(new Zone(z));
                            }
                            Federation fedAfterFree= new Federation(restZones);

                            // apply guards, then filter out all zones that are not satisfiable.
                            satisfiableZones = new ArrayList<Zone>();
                            oneZoneIsSatisfiable = false;
                            System.out.println("Zone size" + fedAfterReset.getZones().size() + " " + fedAfterFree.getZones().size());
                            for (Zone z : fedAfterFree.getZones()) {
                                for (Guard g: e.getGuards()) {
                                    z.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(),clocks) );
                                }
                                if (DBMLib.dbm_isEmpty(z.getDbm(),clocks.size()+1))
                                {
                                    System.out.println("found an empty zone");
                                    // Adding the guards would violate this zone, so we can remove it, i.e. not add it to the list of zones we take over
                                }
                                else
                                {
                                    oneZoneIsSatisfiable= true;
                                    satisfiableZones.add(z);
                                }
                            }
                            if (oneZoneIsSatisfiable)
                            {
                                System.out.println("Updating the guard now");
                                Federation fedAfterGuards = new Federation(satisfiableZones);
                                // build constraints for strengthening the guard to exclude the inc. part.
                                List<Guard> newGuard = new ArrayList<Guard>();
                                newGuard.addAll(e.getGuards());
                                for (Zone z : fedAfterFree.getZones()) {

                                    List<Guard> zoneConstraints = z.buildGuardsFromZone(clocks);
                                    newGuard.addAll(zoneConstraints.stream().map(Guard::negate).collect(Collectors.toList()));

                                }
                                e.setGuards(newGuard);
                                System.out.println("Done updating the guard");
                            }
                            else // none of the zones was satisfiable, so the transition cannot go to a bad state anyway
                            {
                                System.out.println("Guards were not satisfiable");
                               // edges.remove(e);
                            }

                        }
                        else // none of the zones was satisfiable, so the transition cannot go to a bad state anyway
                        {
                            System.out.println("Resets were not satisfiable");

                            //edges.remove(e);
                        } */

                    }

                    // we need to add all the locations that could have been "saved" by this transition back to the list of inconsistent locations, because they might not be saved anymore now
                    // i.e., when we had an input transition leading to an inconsistent location, we might have created a predt federation based on the output we just removed or restricted, so we need to do it again
                    for (Edge e_i : edges.stream().filter(e_i -> e_i.getSource().equals(e.getSource()) && e_i.isInput() && e_i.getTarget().isInconsistent()).collect(Collectors.toList())){
                        inc.add(e_i.getTarget());
                    }

                }
                else { // treating inputs now

                    if (printComments)
                        System.out.println("Handling an input to inc.");

                    // first we need to get the zone that leads to the inconsistent location.
                    // This means making a zone of its invariant, then free the clocks that are updated, and finally we include the zones of the guard

                    Federation incFederation;
                    if (e.getTarget().getInconsistentPart()==null) {
                        if (printComments)
                            System.out.println("The target location is completely inconsistent");
                        Zone incZone = new Zone(clocks.size() + 1, true);
                        incZone.init();
                        List<Zone> zoneList = new ArrayList<Zone>();
                        zoneList.add(incZone);
                        incFederation = new Federation(zoneList);
                    }
                    else
                    {
                        incFederation=e.getTarget().getInconsistentPart();
                    }




                    // TODO: do not add the inconsistent part to the invariant immediately. Keep track of it immediately!

                    // TODO: Do I need to apply the target invariant?? It should mostly be a negation of the inc. part, so it might always be false?
                    // My best guess at an answer: I need to keep the negation of the inconsistent part in a "separate invariant", that I keep track of and only add in the end.
                    // Because I assume that here I should apply the original invariant, but not the negated inconsistent part (that would always make the federation to the inconsistent part false)
                    // The original invariant might already keep me from reaching the inconsistent part, so I cannot throw that information away. // TODO: Important

                    // apply target invariant // TODO: do this once the inconsistent part is not added to invariants anymore
//                        for (Guard g : e.getTarget).getInvariant())
//                        {
//                            incZone.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
//                        }



                    // apply updates as guard
                    for (Zone z : incFederation.getZones()) {
                        for (Update u : e.getUpdates()) {

                            z.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), false), getIndexOfClock(u.getClock(), clocks));
                        }
                    }

                    if (!incFederation.isValid())
                    {
                        // Checking for satisfiability after clocks were reset (only a problem because target invariant might be x>4)
                        // if unsatisfiable => keep edge // todo: is continue the right thing here?
                        continue;
                    }

                    if (printComments)
                        System.out.println("Updates as guards done");

                    // apply updates via free
                    for (Zone z : incFederation.getZones()) {
                        for (Update u : e.getUpdates()) {
                            z = z.freeClock(getIndexOfClock(u.getClock(), clocks));
                        }
                    }

                    if (printComments)
                        System.out.println("Updates via free done");

                    // apply guards
                    List<Zone> newZoneList = new ArrayList<>();
                    for (Zone z : incFederation.getZones()) {
                        for (List<Guard> lg : e.getGuards()) {
                            Zone copy = new Zone(z.getDbm());
                            for (Guard g : lg)
                                copy.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                            newZoneList.add(copy);
                        }
                    }
                    incFederation = new Federation(newZoneList);
                    if (printComments)
                        System.out.println("Guards done");


                    // apply source invariant

/*                        for (List<Guard> list : e.getSource().getInvariant()) {
                            for (Guard g : list) {
                                incZone.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                            }
                        }*/


                    Federation invarFed = e.getSource().getInvariantFederation(clocks);
                    incFederation = invarFed.intersect(incFederation);

                    if (printComments)
                        System.out.println("Invariants done");

                    // turn that zone into a federation, so that we can use it later
                    if (!incFederation.isEmpty()) {
                        if (printComments)
                            System.out.println("Built the federation for the target location");
                    }
                    else
                    {
                        if (printComments)
                            System.out.println("zone was not satisfiable, creating an empty federation");
                    }
                    //  }

/*
                    else
                    {   // TODO: Remove duplicate code! ==> hopefully done now
                        if (printComments)
                            System.out.println("The target location is only partially inconsistent.");
                        List<Zone> zoneList = new ArrayList<Zone>();
                        for (Zone z: e.getTarget().getInconsistentPart().getZones())
                        {
                            if (!DBMLib.dbm_isEmpty(z.getDbm(), clocks.size()+1)) {
                                Zone incZone = new Zone(z);
                                incZone.close();


                                // TODO: apply changes here, if you fix the TODOs above.
                                // apply updates as guard
                                for (Update u : e.getUpdates()) {
                                    if (!DBMLib.dbm_isEmpty(incZone.getDbm(), clocks.size() + 1))
                                        incZone.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), false), getIndexOfClock(u.getClock(),clocks) );
                                }

                                // apply updates via free
                                for (Update u : e.getUpdates()) {
                                    if (!DBMLib.dbm_isEmpty(incZone.getDbm(), clocks.size() + 1)) {
                                        incZone = incZone.freeClock(getIndexOfClock(u.getClock(), clocks));
                                    }
                                }

                                // apply guards
                                for (Guard g : e.getGuards()) {
                                    if (!DBMLib.dbm_isEmpty(incZone.getDbm(), clocks.size() + 1))
                                        incZone.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                                }

                                // apply source invariant
                                for (Guard g : e.getSource().getInvariant())
                                {
                                    if (!DBMLib.dbm_isEmpty(incZone.getDbm(), clocks.size() + 1))
                                        incZone.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                                }

                                // turn that zone into a federation, so that we can use it later
                                if (!DBMLib.dbm_isEmpty(incZone.getDbm(), clocks.size() + 1)) {
                                    incZone.close();
                                    zoneList.add(incZone);
                                    }
                                }
                            }

                        incFederation = new Federation(zoneList);
                    }*//*


                     */

                    // for each transition leaving e.Source that is not a selfloop or leading to inc, we take its delay.
                    // If the intersection of its delay and the federation we got from the transition leading to the inconsistent location is empty
                    // then we know that delay of that transition is a consistent part of e.source.
                    // We add this consistent part to the invariant.
                    // We DO NOT remove e.source in the steps below, but need to backtrack the guard of the transition to inc (even if the predt. part does not backtrack).
/*

                    boolean needToBacktrack = false;
                    for (Edge otherEdge : edges.stream().filter(o -> o.getSource().equals(e.getSource()) && !o.getTarget().equals(e.getSource()) && !o.getTarget().isInconsistent()).collect(Collectors.toList())) {
                        Zone guardZone = new Zone(clocks.size() + 1, true);
                        Zone delay = new Zone(guardZone.delayNewDBM());
                        List<Zone> zoneListDelay = new ArrayList<Zone>() {
                        };
                        zoneListDelay.add(delay);
                        Federation delayFederation = new Federation(zoneListDelay);
                        if (!delayFederation.intersects(incFederation))
                            needToBacktrack = true;
                    }

                    Zone everything = new Zone(clocks.size() + 1, true); // TODO: Check whether an empty zone really is "everything" => completely unrestricted
                    List<Zone> zoneListEverything = new ArrayList<Zone>() {
                    };
                    zoneListEverything.add(everything);
                    Federation everythingFed = new Federation(zoneListEverything);
                    Federation federationToBeBacktracked = Federation.predt(everythingFed, incFederation);
*/

/*

                    // if the inconsistent part cannot be reached, we can ignore the edge e, and go on
                    if (incFederation.size()==0)
                    {
                        if (printComments)
                            System.out.println("could not reach inconsistent part, fed is empty");
                    }
                    else {

                        // in the next step, we need to check whether there is output transitions that could lead us away from the inconsistent state
                        // such a transition needs to
                        // a) have the same source as e
                        // b) not be a selfloop  TODO: not be a selfloop without clock resets and guards? Or simply remove that condition?
                        // c) be an output
                        // d) not lead to an inconsistent state itself TODO: removed  "&& !o.isInput()", since we revisit the location anyway. Check if this is correct.

*/
                    // if such a transition does not exist,
                   // if (edges.stream().filter(o -> o.getSource().equals(e.getSource()) /*&& !o.getTarget().equals(e.getSource())*/ && !o.getTarget().isInconsistent() && !o.isInput()).collect(Collectors.toList()).isEmpty()) {
 /*                       // TODO include selfloops
                        if (needToBacktrack)
                        {
                            // TODO: Backtrack
                            Location current = e.getSource();
                            boolean cont = true;
                            HashSet<Edge> incomingTransitions = new HashSet<>();
                            HashSet<Edge> processedTransitions = new HashSet<>();
                            incomingTransitions.addAll(edges.stream().filter(o -> o.getTarget().equals(current) && !o.getTarget().equals(o.getSource())).collect(Collectors.toList()));

                            while (cont == true)
                            {
                                Edge currentEdge = incomingTransitions.stream().collect(Collectors.toList()).get(0);
                                processedTransitions.add(currentEdge);
                                incomingTransitions.remove(currentEdge);
                                current = currentEdge.getSource();




                                boolean atLeastOneZoneIsSatisfiable = false;
                                for (Zone z : federationToBeBacktracked.getZones()) // TODO: I'm checking if at least one Zone is not empty. Is this the right way?
                                    if (!DBMLib.dbm_isEmpty(z.getDbm(), clocks.size() + 1))
                                        atLeastOneZoneIsSatisfiable = true;
                                if (!atLeastOneZoneIsSatisfiable) {
                                    cont = false;
                                }
                                if (current.isInitial())
                                    cont = false;
                            }

                        }
                        else {
                            edges.remove(e);
                            e.getSource().setInconsistent(true);
                            inc.add(e.getSource());
                        }
                    } else {*/

                        // we keep a copy of the inc. Federation, so we can do comparison to it later
                        Federation save = new Federation(incFederation.getZones());
                        // for each "good" transition, we remove its zone from the zone leading to inc. via the predt function
                    // TODO: i removed selfloops from this for loop => is there a need to treat this specifically?
                        for (Edge otherEdge : edges.stream().filter(o -> o.getSource().equals(e.getSource()) && /*!o.getTarget().equals(e.getSource()) && */!o.getTarget().isInconsistent()).collect(Collectors.toList())) {
                            if (printComments)
                                System.out.println("found an output that might lead us to good");
                            // Create an empty zone
                            Zone zoneGuard = new Zone(clocks.size() + 1, true);
                            zoneGuard.init();

                            // turn it into a federation
                            List<Zone> zoneListGuard = new ArrayList<Zone>();
                            zoneListGuard.add(zoneGuard);
                            Federation goodFed = new Federation(zoneListGuard);

                            // constrain it by the guards and invariants  of the "good transition".
                            Federation targetInvFed = otherEdge.getTarget().getInvariantFederation(clocks);
                            goodFed = targetInvFed.intersect(goodFed);

                            for (Update u: otherEdge.getUpdates())
                                goodFed.free(getIndexOfClock(u.getClock(),clocks));

                            Federation sourceInvFed = otherEdge.getSource().getInvariantFederation(clocks);
                            goodFed = sourceInvFed.intersect(goodFed);

                            List<Zone> newZoneList1 = new ArrayList<>();
                            for (Zone z: goodFed.getZones()) {
                                for (List<Guard> gl : otherEdge.getGuards()) {
                                    Zone copy = new Zone(z);
                                    for (Guard g : gl) {
                                        copy.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));

                                    }
                                    newZoneList1.add(copy);

                                }
                            }
                            goodFed = new Federation(newZoneList1);
                            // now we have the zone that can save us, so turn it into a federation


                            // do predt.
                            Federation predtFed = Federation.predt(incFederation, goodFed);

                            // add the inconsistent Federation to it, so in case both the transition to bad and the transition to good
                            // have the guard x>4, we still get the bad zone in the result
                            if (predtFed.getZones().size() == 0)
                                incFederation = incFederation; // ==> do nothing, will be replaced by incFederation.down anyway
                            else
                                incFederation = Federation.fedPlusFed(predtFed, incFederation);
                        }

                        // if the bad federation was not restricted via a good transition (i.e., its the same as before)
                        // we have to take its past into the federation, as ending up in its past is already dooming us
                         if (Federation.fedEqFed(incFederation, save)) { // TODO: check that
                        //if (edges.stream().filter(o -> o.getSource().equals(e.getSource()) && !o.getTarget().equals(e.getSource()) && !o.getTarget().isInconsistent() && !o.isInput()).collect(Collectors.toList()).isEmpty()) {
                            if (printComments)
                                System.out.println("Could not be saved by an output");

                            incFederation = incFederation.down(); // TODO: Check if this works
                        }

                        if (printComments)
                            System.out.println("Did the predt stuff");


                        // Now we have the federation that can lead to inc.
                        // If that federation is unsatisfiable, we can just ignore the transition to inc, and be done,
                        // so we check for that, zone by zone. Only one zone needs to be sat.
                        List<Zone> zonesOfFederation = incFederation.getZones();
                        boolean atLeastOneZoneIsSatisfiable = false;
                        for (Zone z : zonesOfFederation)
                            if (!DBMLib.dbm_isEmpty(z.getDbm(), clocks.size() + 1))
                                atLeastOneZoneIsSatisfiable = true;
                        if (!atLeastOneZoneIsSatisfiable) {
                            // we can ignore e
                        } else
                        // if the federation is satisfiable, we need to add the NEGATION of each of its zones to the invariant of the source of e.
                        { // TODO: this is where i should change it to not restricting the source invariant yet, but just storing the inconsistent part
                            boolean completelyInconsistent = false;
                            if (printComments)
                                System.out.println("There is a satisfiable federation leading to BAD, Fed Size: "  + incFederation.size());
                            for (Zone z : incFederation.getZones()) {
                                if (z.buildGuardsFromZone(clocks).isEmpty()) {
                                    if (printComments)
                                        System.out.println("No guard was built");
                                    // the complete clock space is inconsistent.
                                    // This means we need to set the invariant to false and the inconsistent part to null
                                    List<Guard> temp = new ArrayList<>();
//                                    temp.add(List<Guard> {e.getSource().getInvariant()});
                                    completelyInconsistent =true;
                                    // Setting the invariant to false by adding infeasible constraints.
                                    // TODO: This way of settng a guard to false only works if there is at least one clock! Is there a nicer way to handle this?
                                    assert (clocks.size()>0);
                                    temp.add(new Guard(clocks.get(0), 1,0,true) );
                                    temp.add(new Guard(clocks.get(0), 0,1,true) );

                                    // we do not modify the invariant at this point anymore, but will add the negated inconclusive part in the end!
//                                    e.getSource().setInvariant(e.getSource().getInvariant().);
                                }

                                // we build the guards for the current zone of the federation and add its negation to the invariant.
                                // TODO: THIS IS WRONG!!!!!
                                // a federation is a disjunction of zones, each zone is a conjunction of guards
                                // TODO: the conjunction turns into disjunction, and I don't know how to handle this!!!!!!
                                for (Guard g : z.buildGuardsFromZone(clocks)) {
                                    List<Guard> temp = new ArrayList<>();
//                                    temp.addAll(e.getSource().getInvariant());
//                                    temp.add(g.negate());
//                                    e.getSource().setInvariant(temp);
                                }
                                // we also need to set this location as inconsistent, so that we can go further back along incoming inputs
                                e.getSource().setInconsistent(true);
                                if (completelyInconsistent)
                                    e.getSource().setInconsistentPart(null);
                                else
                                    e.getSource().setInconsistentPart(incFederation);

                                // check whether we need to add the new source location
                                if (passedPairs.containsKey(e.getSource()) &&  Federation.fedEqFed(passedPairs.get(e.getSource()),incFederation) ){
                                    // location and federation already processed
                                }
                                else
                                {
                                    passedPairs.put(e.getSource(),incFederation);
                                    inc.add(e.getSource());
                                }


                                if (e.getSource().isInitial()) {
                                    if (printComments)
                                        System.out.println("Initial Location is inconsistent!");
                                    //edges.clear();
                                    //locations.clear();
                                    //locations.add(e.getSource());
                                }
                            }
                        }





                        //DBMLib.dbm_freeClock()
                        //Federation fed = Federation.dbmMinusDbm(zone, zoneGuard);



                }

                if (printComments)
                    System.out.println("Removing transition if its not satisfiable anymore");
                Zone testForSatEdgeZone = new Zone(clocks.size()+1, true);
                testForSatEdgeZone.init();
                List<Zone> zoneList = new ArrayList<>();
                zoneList.add(testForSatEdgeZone);
                Federation testForSatEdgeFed = new Federation(zoneList);



/*
                for (Guard g: e.getTarget().getInvariant()) {
                    Guard g1 = new Guard(g.getClock(),g.getUpperBound(),g.getLowerBound(),false);
                    if (!DBMLib.dbm_isEmpty(testForSatEdgeZone.getDbm(), clocks.size() + 1))
                        testForSatEdgeZone.buildConstraintsForGuard(g1, getIndexOfClock(g1.getClock(), clocks));

                }
*/

                Federation tartgetInvFed = e.getTarget().getInvariantFederation(clocks);
                testForSatEdgeFed = tartgetInvFed.intersect(testForSatEdgeFed);

                // apply updates as guard
                for (Zone z : testForSatEdgeFed.getZones()) {
                    for (Update u : e.getUpdates()) {
                        if (!DBMLib.dbm_isEmpty(testForSatEdgeZone.getDbm(), clocks.size() + 1))
                            z.buildConstraintsForGuard(new Guard(u.getClock(), u.getValue(), u.getValue(), false), getIndexOfClock(u.getClock(), clocks));
                    }
                }


                // apply updates via free
                for (Zone z : testForSatEdgeFed.getZones()) {

                    for (Update u : e.getUpdates()) {
                        if (!DBMLib.dbm_isEmpty(testForSatEdgeZone.getDbm(), clocks.size() + 1))
                            z = testForSatEdgeZone.freeClock(getIndexOfClock(u.getClock(), clocks));
                    }
                }

                // apply guards
                List<Zone> zoneList1 = new ArrayList<>();
                for (Zone z : testForSatEdgeFed.getZones()) {
                    for (List<Guard> gl : e.getGuards()) {
                        Zone copy = new Zone(z.getDbm());
                        for (Guard g : gl) {

                        //    if (!DBMLib.dbm_isEmpty(testForSatEdgeZone.getDbm(), clocks.size() + 1))
                                copy.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                        }
                        zoneList1.add(copy);
                    }
                }
                testForSatEdgeFed = new Federation(zoneList1);



                // apply source invariant

/*                for (Guard g : e.getSource().getInvariant())
                {
                    if (!DBMLib.dbm_isEmpty(testForSatEdgeZone.getDbm(), clocks.size() + 1))
                        testForSatEdgeZone.buildConstraintsForGuard(g, getIndexOfClock(g.getClock(), clocks));
                }*/


                Federation sourceInvFed = e.getSource().getInvariantFederation(clocks);
                testForSatEdgeFed = sourceInvFed.intersect(testForSatEdgeFed);



                if (testForSatEdgeFed.isEmpty()) {
                    edges.remove(e);
                }
                if (printComments)
                    System.out.println("... done");


            }


            inc.remove(finalInc);
        }

        Automaton aut = new Automaton(getName(), locations, edges, getClocks(), false);
        return new SimpleTransitionSystem(aut);

    }



}
