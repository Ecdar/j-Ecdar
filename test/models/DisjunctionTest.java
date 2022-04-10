package models;

import Exceptions.CddAlreadyRunningException;
import Exceptions.CddNotRunningException;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.List;

public class DisjunctionTest {

    private static Automaton expected, actual;
    private static Update[] noUpdate = new Update[]{};
    private static List<List<Guard>> noguard = new ArrayList<>();


    static Automaton[] automata;
    private static TransitionSystem D1,D2;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        automata = XMLParser.parse("./samples/xml/DisjunctionTests.xml", true);
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(automata[0].getClocks());
        clocks.addAll(automata[1].getClocks());
        CDD.addClocks(clocks);
        D1 = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[0]));
        D2 = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[1]));


    }

    @Test
    public void testRef() {

        assert(new Refinement(D1, D2).check() && new Refinement(D2, D1).check());


    }

    @Test
    public void testSafeLoadRef() {

        ((SimpleTransitionSystem)D1).toXML("D1_test.xml");
        ((SimpleTransitionSystem)D2).toXML("D2_test.xml");

        Automaton[] automata1 = XMLParser.parse("D1_test.xml", false);
        Automaton[] automata2 = XMLParser.parse("D2_test.xml", false);
        CDD.done();
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        //clocks.addAll(D1.getClocks());
        //clocks.addAll(D2.getClocks());
        clocks.addAll(automata1[0].getClocks());
        clocks.addAll(automata2[0].getClocks());
        CDD.addClocks(clocks);

        D1 = new SimpleTransitionSystem(automata1[0]);
        D2 = new SimpleTransitionSystem(automata2[0]);
        assert(new Refinement(D1, D2).check());
        assert(new Refinement(D2, D1).check());


    }


    @Test
    public void testGuardNegation() {
        Clock x = new Clock("x");
        Guard g1 = new ClockGuard(x, null, 4, Relation.GREATER_THAN );  //x>4
        Guard g2 = new ClockGuard(x, null, 5, Relation.LESS_THAN); //x<5
        Guard g3 = new ClockGuard(x, null, 7,Relation.LESS_THAN); //x<7
        Guard g4 = new ClockGuard(x, null, 3,Relation.GREATER_THAN); //x>3
        Guard g5 = new ClockGuard(x, null, 6,Relation.GREATER_THAN); //x>6
        Guard g6 = new ClockGuard(x, null, 8,Relation.GREATER_THAN); //x>8
        Guard g7 = new ClockGuard(x, null, 9,Relation.GREATER_EQUAL); //x>9

        List<Guard> disj1 = new ArrayList<>();
        disj1.add(g1);
        disj1.add(g2);
        List<List<Guard>> dis1 = new ArrayList<>();
        dis1.add(disj1);
        List<Guard> disj2 = new ArrayList<>();
        disj2.add(g3);
        disj2.add(g4);
        List<List<Guard>> dis2 = new ArrayList<>();
        dis2.add(disj2);
        List<Guard> disj3 = new ArrayList<>();
        disj3.add(g5);
        disj3.add(g6);
        disj3.add(g7);
        List<List<Guard>> dis3 = new ArrayList<>();
        dis3.add(disj3);

        CDD.done();
        CDD.init(1000,1000,1000);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        CDD.addClocks(clocks);
        System.out.println("here");

        CDD disjunctedGuards = CDD.cddFalse();
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis1));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis2));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis3));
        disjunctedGuards.printDot();
        CDD neg = disjunctedGuards.negation();
        System.out.println("too");
        List<List<Guard>> out = CDD.toGuards(neg);


        System.out.println(disjunctedGuards);
        System.out.println(out);


        assert(out.toString().equals("[[Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}], " +
                "[Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}]]"));


    }



}
