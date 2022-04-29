package features;

import lib.CDDLib;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.*;
import org.junit.Test;
import parser.XMLFileWriter;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BoolTest {


    @Test
    public void testBoolArraySimple() {

        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        BoolVar c = new BoolVar("c",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b); BVs.add(c);

        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addBddvar(BVs);
        CDD ba = CDD.createBddNode(0);
        CDD bb = CDD.createBddNode(1);
        CDD bc = CDD.createBddNode(2);
        CDD cdd =ba.disjunction(bb.conjunction(bc));
        System.out.println("size " + BVs.size());
        BDDArrays bddArr = new BDDArrays(CDDLib.bddToArray(cdd.getPointer(),BVs.size()));

        System.out.println(bddArr.toString());

        System.out.println(cdd);
        //      assert(cdd.toString().equals("[[(a==true), (b==false), (c==false)], [(a==true), (b==true), (c==false)], [(a==false), (b==true), (c==false)]]"));
        CDD.done();
    }

    @Test
    public void testBoolArray() {

        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        BoolVar c = new BoolVar("c",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b); BVs.add(c);
        BoolGuard bg_a_false = new BoolGuard(a, "==",false);
        BoolGuard bg_b_false = new BoolGuard(b, "==",false);
        BoolGuard bg_a_true = new BoolGuard(a, "==",true);
        BoolGuard bg_b_true = new BoolGuard(b, "==",true);
        BoolGuard bg_c_true = new BoolGuard(c, "==",true);
        BoolGuard bg_c_false = new BoolGuard(c, "==",false);
        List<Guard> l1 = new ArrayList<>(List.of(bg_a_true,bg_b_false,bg_c_false));
        //  List<Guard> l2 = new ArrayList<>(List.of(bg_a_true,bg_b_true,bg_c_false));
        //  List<Guard> l3 = new ArrayList<>(List.of(bg_a_false,bg_b_true,bg_c_false));
        List<List<Guard>> list = new ArrayList();
        list.add(l1); //list.add(l2); list.add(l3);
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addBddvar(BVs);
        CDD cdd =new CDD(new AndGuard(l1));
        BDDArrays bddArr = new BDDArrays(CDDLib.bddToArray(cdd.getPointer(),BVs.size()));
        System.out.println(bddArr.getValues());
        System.out.println(bddArr.getVars());

        // A & !B & !C
        System.out.println("here too! " + cdd);
  //      assert(cdd.toString().equals("[[(a==true), (b==false), (c==false)], [(a==true), (b==true), (c==false)], [(a==false), (b==true), (c==false)]]"));
        CDD.done();
    }


    @Test
    public void testBooleanSimplification() {

        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        BoolVar c = new BoolVar("c",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b); BVs.add(c);
        BoolGuard bg_a_false = new BoolGuard(a, "==",false);
        BoolGuard bg_b_false = new BoolGuard(b, "==",false);
        BoolGuard bg_a_true = new BoolGuard(a, "==",true);
        BoolGuard bg_b_true = new BoolGuard(b, "==",true);
        BoolGuard bg_c_true = new BoolGuard(c, "==",true);
        BoolGuard bg_c_false = new BoolGuard(c, "==",false);
        Guard l1 = new AndGuard(bg_a_true,bg_b_false,bg_c_false);
        Guard l2 = new AndGuard(bg_a_true,bg_b_true,bg_c_false);
        Guard l3 = new AndGuard(bg_a_false,bg_b_true,bg_c_false);
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addBddvar(BVs);
        System.out.println("or guard " + new OrGuard(l1,l2,l3));
        CDD cdd =new CDD(new OrGuard(l1,l2,l3));
        cdd.printDot();
        System.out.println( l1 + "  " +  l2 + "  " +  l3 + "  " + cdd);
        //assert(cdd.toString().equals("[[(a==true), (b==false), (c==false)], [(a==true), (b==true), (c==false)], [(a==false), (b==true), (c==false)]]"));
        CDD.done();
    }

    @Test
    public void testTwoEdgesWithDifferentBool() {
        Clock x = new Clock("x");
        Clock y = new Clock("y");
        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b);

        List<Update> noUpdate = new ArrayList<>();
        List<List<Guard>> noguard = new ArrayList<>();
        List<Guard> emptyBoolGuards = new ArrayList<>();
        BoolUpdate[] emptyBoolUpdates = new BoolUpdate[]{};

        ClockGuard g1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);
        ClockGuard g2 = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);
        ClockGuard g4 = new ClockGuard(y, 2, Relation.GREATER_EQUAL);

        ClockGuard g5= new ClockGuard(x, 6, Relation.LESS_EQUAL);
        ClockGuard g6 = new ClockGuard(x, 1, Relation.GREATER_EQUAL);
        ClockGuard g7 = new ClockGuard(y, 7, Relation.LESS_EQUAL);
        ClockGuard g8 = new ClockGuard(y, 6, Relation.GREATER_EQUAL);


        BoolGuard bg1 = new BoolGuard(a, "==",false);
        BoolGuard bg2 = new BoolGuard(b, "==",false);
        List<Guard> boolGuards1 = new ArrayList<>();
        List<Guard> boolGuards2 = new ArrayList<>();
        boolGuards1.add(bg1);
        boolGuards2.add(bg2);

        List<List<Guard>> guards1 = new ArrayList<>();
        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g2);
        inner.add(g3);
        inner.add(g4);
        inner.addAll(boolGuards1);
        guards1.add(inner);

        List<List<Guard>> guards2 = new ArrayList<>();
        List<Guard> inner1 = new ArrayList<>();
        inner1.add(g5);
        inner1.add(g6);
        inner1.add(g7);
        inner1.add(g8);
        inner1.addAll(boolGuards2);
        guards2.add(inner1);

        Location l0 = new Location("L0", new TrueGuard(), true, false, false, false);
        Location l1 = new Location("L1", new TrueGuard(), false, false, false, false);

        Channel i1 = new Channel("i1");

        Edge e0 = new Edge(l0, l1, i1, true, new AndGuard(inner), noUpdate);
        Edge e1 = new Edge(l0, l1, i1, true, new AndGuard(inner1), noUpdate);

        List<Location> locations = new ArrayList<>();
        locations.add(l0);
        locations.add(l1);

        List<Edge> edges = new ArrayList<>();
        edges.add(e0);
        edges.add(e1);
        System.out.println(e0);
        System.out.println(e1);

        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        List<BoolVar> bools = new ArrayList<>();
        bools.add(a);
        bools.add(b);
        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks(clocks);
        CDD.addBddvar(BVs);
        CDD origin1 = new CDD(new AndGuard(inner));
        CDD origin2 = new CDD(new AndGuard(inner1));
        CDD bothOrigins = origin1.disjunction(origin2);

        Automaton aut = new Automaton("Automaton", locations, edges, clocks, bools,false);

        XMLFileWriter.toXML("booltest1.xml",new SimpleTransitionSystem(aut));
        CDD.done();
        assert(true);


//here: [cg: (x<1 && y≥2 && y≤7) || (x≤10 && y<2) || (x≤10 && y>7) || (x>10) - bg:()]
    }


    @Test
    public void testOverlappingZonesWithDifferentBool() {
        Clock x = new Clock("x");
        Clock y = new Clock("y");
        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        List<BoolVar> bools = new ArrayList<>();
        bools.add(a);
        bools.add(b);

        List<Update> noUpdate = new ArrayList<>();
        List<List<Guard>> noguard = new ArrayList<>();

        ClockGuard g1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);
        ClockGuard g2 = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);
        ClockGuard g4 = new ClockGuard(y, 2, Relation.GREATER_EQUAL);

        ClockGuard g5= new ClockGuard(x, 6, Relation.LESS_EQUAL);
        ClockGuard g6 = new ClockGuard(x, 1, Relation.GREATER_EQUAL);
        ClockGuard g7 = new ClockGuard(y, 7, Relation.LESS_EQUAL);
        ClockGuard g8 = new ClockGuard(y, 6, Relation.GREATER_EQUAL);


        BoolGuard bg_a_false = new BoolGuard(a, "==",false);
        BoolGuard bg_b_false = new BoolGuard(b, "==",false);
        BoolGuard bg_a_true = new BoolGuard(a, "==",true);
        BoolGuard bg_b_true = new BoolGuard(b, "==",true);

        List<Guard> boolGuards1 = new ArrayList<>();
        List<Guard> boolGuards2 = new ArrayList<>();
        List<Guard> boolGuards3 = new ArrayList<>();
        boolGuards1.add(bg_a_false);
        boolGuards1.add(bg_b_false);

        boolGuards2.add(bg_a_false);
        boolGuards2.add(bg_a_true);

        boolGuards3.add(bg_a_true);
        boolGuards3.add(bg_a_true);


        List<List<Guard>> guards1 = new ArrayList<>();
        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g2);
        inner.add(g3);
        inner.add(g4);
        inner.addAll(boolGuards1);
        guards1.add(inner);

        List<List<Guard>> guards2 = new ArrayList<>();
        List<Guard> inner1 = new ArrayList<>();
        inner1.add(g5);
        inner1.add(g6);
        inner1.add(g7);
        inner1.add(g8);
        inner1.addAll(boolGuards2);
        guards2.add(inner1);

        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks();
        CDD.addBddvar(BVs);
        CDD compl = (new CDD(new AndGuard(inner)).disjunction(new CDD(new AndGuard(inner1)))).negation();


        Location l0 = new Location("L0", new TrueGuard(), true, false, false, false);
        Location l1 = new Location("L1", new TrueGuard(), false, false, false, false);

        Channel i1 = new Channel("i1");

        Edge e0 = new Edge(l0, l1, i1, true, new AndGuard(inner), noUpdate);
        Edge e1 = new Edge(l0, l1, i1, true, new AndGuard(inner1), noUpdate);
        Edge e2 = new Edge(l0, l1, i1, true, CDD.toGuardList(compl,clocks), noUpdate);

        List<Location> locations = new ArrayList<>();
        locations.add(l0);
        locations.add(l1);

        List<Edge> edges = new ArrayList<>();
        edges.add(e0);
        edges.add(e1);
        edges.add(e2);
        System.out.println(e0);
        System.out.println(e1);





        Automaton aut = new Automaton("Automaton", locations, edges, clocks, bools,false);
        XMLFileWriter.toXML("booltest2.xml",new SimpleTransitionSystem(aut));
        assert(new SimpleTransitionSystem(aut).isDeterministic()); // no idea if it is...
        CDD.done();
        assert(true);
//here: [cg: (x<1 && y≥2 && y≤7) || (x≤10 && y<2) || (x≤10 && y>7) || (x>10) - bg:()]
    }


    @Test
    public void sameButNowMakeInputEnabled() {
        Clock x = new Clock("x");
        Clock y = new Clock("y");
        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        List<BoolVar> bools = new ArrayList<>();
        bools.add(a);
        bools.add(b);

        List<Update> noUpdate = new ArrayList<>();
        List<List<Guard>> noguard = new ArrayList<>();

        ClockGuard g1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);
        ClockGuard g2 = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);
        ClockGuard g4 = new ClockGuard(y, 2, Relation.GREATER_EQUAL);

        ClockGuard g5= new ClockGuard(x, 6, Relation.LESS_EQUAL);
        ClockGuard g6 = new ClockGuard(x, 1, Relation.GREATER_EQUAL);
        ClockGuard g7 = new ClockGuard(y, 7, Relation.LESS_EQUAL);
        ClockGuard g8 = new ClockGuard(y, 6, Relation.GREATER_EQUAL);


        BoolGuard bg_a_false = new BoolGuard(a, "==",false);
        BoolGuard bg_b_false = new BoolGuard(b, "==",false);
        BoolGuard bg_a_true = new BoolGuard(a, "==",true);
        BoolGuard bg_b_true = new BoolGuard(b, "==",true);

        List<Guard> boolGuards1 = new ArrayList<>();
        List<Guard> boolGuards2 = new ArrayList<>();
        List<Guard> boolGuards3 = new ArrayList<>();
        boolGuards1.add(bg_a_false);
        boolGuards1.add(bg_b_false);

        boolGuards2.add(bg_a_false);
        boolGuards2.add(bg_a_true);

        boolGuards3.add(bg_a_true);
        boolGuards3.add(bg_a_true);


        List<List<Guard>> guards1 = new ArrayList<>();
        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g2);
        inner.add(g3);
        inner.add(g4);
        inner.addAll(boolGuards1);
        guards1.add(inner);

        List<List<Guard>> guards2 = new ArrayList<>();
        List<Guard> inner1 = new ArrayList<>();
        inner1.add(g5);
        inner1.add(g6);
        inner1.add(g7);
        inner1.add(g8);
        inner1.addAll(boolGuards2);
        guards2.add(inner1);

        CDD.init(CDD.maxSize,CDD.cs,CDD.stackSize);
        CDD.addClocks();
        CDD.addBddvar(BVs);
        CDD compl = (new CDD(new AndGuard(inner)).disjunction(new CDD(new AndGuard(inner1)))).negation();


        Location l0 = new Location("L0", new TrueGuard(), true, false, false, false);
        Location l1 = new Location("L1", new TrueGuard(), false, false, false, false);

        Channel i1 = new Channel("i1");

        Edge e0 = new Edge(l0, l1, i1, true, new AndGuard(inner), noUpdate);
        Edge e1 = new Edge(l0, l1, i1, true, new AndGuard(inner1), noUpdate);
        Edge e2 = new Edge(l0, l1, i1, true, CDD.toGuardList(compl,clocks), noUpdate);

        List<Location> locations = new ArrayList<>();
        locations.add(l0);
        locations.add(l1);

        List<Edge> edges = new ArrayList<>();
        edges.add(e0);
        edges.add(e1);
        edges.add(e2);
        System.out.println(e0);
        System.out.println(e1);



        CDD.done();

        Automaton aut = new Automaton("Automaton", locations, edges, clocks, bools,true);
        XMLFileWriter.toXML("booltest2.xml",new SimpleTransitionSystem(aut));
        assert(new SimpleTransitionSystem(aut).isDeterministic()); // no idea if it is...

        assert(true);
//here: [cg: (x<1 && y≥2 && y≤7) || (x≤10 && y<2) || (x≤10 && y>7) || (x>10) - bg:()]
    }



    @Test
    public void testBoolSafeLoadXML() {
        Clock x = new Clock("x");
        Clock y = new Clock("y");
        BoolVar a = new BoolVar("a",false);
        BoolVar b = new BoolVar("b",true);
        List<BoolVar> BVs = new ArrayList<>();
        BVs.add(a); BVs.add(b);


        List<Update> noUpdate = new ArrayList<>();
        List<List<Guard>> noguard = new ArrayList<>();

        ClockGuard g1 = new ClockGuard(x, 10, Relation.LESS_EQUAL);
        ClockGuard g2 = new ClockGuard(x, 5, Relation.GREATER_EQUAL);
        ClockGuard g3 = new ClockGuard(y, 3, Relation.LESS_EQUAL);
        ClockGuard g4 = new ClockGuard(y, 2, Relation.GREATER_EQUAL);

        ClockGuard g5= new ClockGuard(x, 6, Relation.LESS_EQUAL);
        ClockGuard g6 = new ClockGuard(x, 1, Relation.GREATER_EQUAL);
        ClockGuard g7 = new ClockGuard(y, 7, Relation.LESS_EQUAL);
        ClockGuard g8 = new ClockGuard(y, 6, Relation.GREATER_EQUAL);


        BoolGuard bg1 = new BoolGuard(a, "==",false);
        BoolGuard bg2 = new BoolGuard(b, "==",false);
        BoolGuard bg3 = new BoolGuard(a, "==",true);
        BoolGuard bg4 = new BoolGuard(b, "==",true);
        List<Guard> boolGuards1 = new ArrayList<>();
        List<Guard> boolGuards2 = new ArrayList<>();
        boolGuards1.add(bg1);
        boolGuards1.add(bg4);

        boolGuards2.add(bg2);
        boolGuards2.add(bg3);


        List<List<Guard>> guards1 = new ArrayList<>();
        List<Guard> inner = new ArrayList<>();
        inner.add(g1);
        inner.add(g2);
        inner.add(g3);
        inner.add(g4);
        inner.addAll((boolGuards1));
        guards1.add(inner);

        List<List<Guard>> guards2 = new ArrayList<>();
        List<Guard> inner1 = new ArrayList<>();
        inner1.add(g5);
        inner1.add(g6);
        inner1.add(g7);
        inner1.add(g8);
        inner1.addAll(boolGuards2);
        guards2.add(inner1);

        Location l0 = new Location("L0", new TrueGuard(), true, false, false, false);
        Location l1 = new Location("L1", new TrueGuard(), false, false, false, false);

        Channel i1 = new Channel("i1");

        Edge e0 = new Edge(l0, l1, i1, true, new AndGuard(inner), noUpdate);
        Edge e1 = new Edge(l0, l1, i1, true, new AndGuard(inner1), noUpdate);

        List<Location> locations = new ArrayList<>();
        locations.add(l0);
        locations.add(l1);

        List<Edge> edges = new ArrayList<>();
        edges.add(e0);
        edges.add(e1);
        System.out.println(e0);
        System.out.println(e1);

        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        clocks.add(y);
        List<BoolVar> bools = new ArrayList<>();
        bools.add(a);
        bools.add(b);

        Automaton aut = new Automaton("Automaton", locations, edges, clocks, bools,false);
        XMLFileWriter.toXML("BoolAutomaton.xml",new Automaton[]{aut});
        Automaton newAut = XMLParser.parse("boolAutomaton.xml",false)[0];
        XMLFileWriter.toXML("BoolAutomatonNew.xml",new Automaton[]{newAut});
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        assert(new Refinement(new SimpleTransitionSystem(aut),new SimpleTransitionSystem(aut)).check());
        assert(new Refinement(new SimpleTransitionSystem(newAut),new SimpleTransitionSystem(newAut)).check());


        System.out.println(aut.toString());
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(newAut.toString());

        assert(new Refinement(new SimpleTransitionSystem(newAut),new SimpleTransitionSystem(aut)).check());
        assert(new Refinement(new SimpleTransitionSystem(aut),new SimpleTransitionSystem(newAut)).check());


//here: [cg: (x<1 && y≥2 && y≤7) || (x≤10 && y<2) || (x≤10 && y>7) || (x>10) - bg:()]
    }
}
