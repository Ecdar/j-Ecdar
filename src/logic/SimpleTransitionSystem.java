package logic;

import models.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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

                if (checkMovesOverlap(tempTrans)) {
                    return false;
                }

                List<State> toAdd = tempTrans.stream().map(Transition::getTarget).
                        filter(s -> !passedContainsState(s) && !waitingContainsState(s)).collect(Collectors.toList()); // TODO I added waitingConstainsState... Okay??

                toAdd.forEach(e->e.extrapolateMaxBounds(maxBounds));
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

                state1.applyGuards(trans.get(i).getGuardCDD());
                state2.applyGuards(trans.get(j).getGuardCDD());

                trans.get(i).getGuardCDD().printDot();



                System.out.println(trans.get(i).getEdges().get(0).getGuards());
                System.out.println(trans.get(j).getEdges().get(0).getGuards());
                CDD reduced1 = state1.getInvarCDD().removeNegative().reduce();
                CDD reduced2 = state2.getInvarCDD().reduce();

                reduced1.printDot();

                if (state1.getInvarCDD().isNotFalse() && state2.getInvarCDD().isNotFalse()) {
                    if(CDD.intersects(state1.getInvarCDD(),state2.getInvarCDD())) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public boolean isConsistentHelper(boolean canPrune) {
        if (!isDeterministic()) // TODO: this was commented out, I added it again
            return false;

        passed = new ArrayList<>();
        boolean result = checkConsistency(getInitialState(), getInputs(), getOutputs(), canPrune);

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
        if (canPrune && CDD.canDelayIndefinitely(currState.getInvarCDD()))
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
                return CDD.canDelayIndefinitely(currState.getInvarCDD());

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

        int[] maxBounds;
        List<Integer> res = new ArrayList<>();
        res.add(0);
        res.addAll(this.getMaxBounds());
        maxBounds= res.stream().mapToInt(i -> i).toArray();
        state.extrapolateMaxBounds(maxBounds);


        for (State passedState : passed) {
            if (state.getLocation().equals(passedState.getLocation()) &&
                    CDD.isSubset(state.getInvarCDD(),(passedState.getInvarCDD()))) {

                return true;
            }
        }

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
            loc.setAttribute("x",String.valueOf(l.getX()));
            loc.setAttribute("y",String.valueOf(l.getY()));

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
                    if (g.getIsFalse())
                    {
                        guardString = "false";
                    }
                    else {
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
                            guardString += interm;
                        else
                            guardString += " && " + interm;
                        if (!guardString.isEmpty()) i++;
                    }
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


    public void toJson(String filename)
    {
        //String file =" <?xml version="1.0" encoding="utf-8"?><!DOCTYPE nta PUBLIC '-//Uppaal Team//DTD Flat System 1.1//EN' 'http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd'><nta><declaration>// Place global declarations here."

        Element nta=new Element("nta");
        Document doc=new Document();
        Element declaration = new Element("declaration");
        nta.addContent(declaration);

        String decString = "";
        for (Channel c : getInputs())
            decString+= "broadcast chan " + c.getName() + "; ";
        for (Channel c : getOutputs())
            decString+= "broadcast chan " + c.getName() + "; ";


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
            loc.setAttribute("x",String.valueOf(l.getX()));
            loc.setAttribute("y",String.valueOf(l.getY()));

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

        Automaton aut = new Automaton(getName(), locations, edges, getClocks(), false);
        return new SimpleTransitionSystem(aut);
    }



    private int getIndexOfClock(Clock clock, List<Clock> clocks) {
        for (int i = 0; i < clocks.size(); i++){
            if(clock.hashCode() == clocks.get(i).hashCode()) return i+1;
        }
        return 0;
    }




}
