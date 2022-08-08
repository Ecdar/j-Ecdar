package models;

import exceptions.CddAlreadyRunningException;
import exceptions.CddNotRunningException;
import log.Log;
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
    private static TransitionSystem D1, D2;

    @After
    public void afterEachTest() {
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws CddAlreadyRunningException, CddNotRunningException {
        automata = XMLParser.parse("./samples/xml/DisjunctionTests.xml", true);
        D1 = new SimpleTransitionSystem((automata[0]));
        D2 = new SimpleTransitionSystem((automata[1]));
    }

    @Test
    public void testRef() {
        assert (new Refinement(D1, D2).check() && new Refinement(D2, D1).check());


    }

    @Test
    public void testSafeLoadRef() {

        ((SimpleTransitionSystem) D1).toXML("testOutput/D1_test.xml");
        ((SimpleTransitionSystem) D2).toXML("testOutput/D2_test.xml");

        Automaton[] automata1 = XMLParser.parse("testOutput/D1_test.xml", false);
        Automaton[] automata2 = XMLParser.parse("testOutput/D2_test.xml", false);
        CDD.done();
        CDD.init(100, 100, 100);
        List<Clock> clocks = new ArrayList<>();
        //clocks.addAll(D1.getClocks());
        //clocks.addAll(D2.getClocks());
        clocks.addAll(automata1[0].getClocks());
        clocks.addAll(automata2[0].getClocks());
        CDD.addClocks(clocks);

        D1 = new SimpleTransitionSystem(automata1[0]);
        D2 = new SimpleTransitionSystem(automata2[0]);
        assert (new Refinement(D1, D2).check());
        assert (new Refinement(D2, D1).check());


    }


    @Test
    public void testGuardNegation() {
        Clock x = new Clock("x", "Aut");
        Guard g1 = new ClockGuard(x, null, 4, Relation.GREATER_THAN);  //x>4
        Guard g2 = new ClockGuard(x, null, 5, Relation.LESS_THAN); //x<5
        Guard g3 = new ClockGuard(x, null, 7, Relation.LESS_THAN); //x<7
        Guard g4 = new ClockGuard(x, null, 3, Relation.GREATER_THAN); //x>3
        Guard g5 = new ClockGuard(x, null, 6, Relation.GREATER_THAN); //x>6
        Guard g6 = new ClockGuard(x, null, 8, Relation.GREATER_THAN); //x>8
        Guard g7 = new ClockGuard(x, null, 9, Relation.GREATER_EQUAL); //x>9

        List<Guard> disj1 = new ArrayList<>();
        disj1.add(g1);
        disj1.add(g2);
        Guard dis1 = new AndGuard(disj1);
        List<Guard> disj2 = new ArrayList<>();
        disj2.add(g3);
        disj2.add(g4);
        Guard dis2 = new AndGuard(disj2);
        List<Guard> disj3 = new ArrayList<>();
        disj3.add(g5);
        disj3.add(g6);
        disj3.add(g7);
        Guard dis3 = new AndGuard(disj3);


        CDD.done();
        CDD.init(1000, 1000, 1000);
        List<Clock> clocks = new ArrayList<>();
        clocks.add(x);
        CDD.addClocks(clocks);

        CDD disjunctedGuards = CDD.cddFalse();
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis1));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis2));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis3));
        CDD neg = disjunctedGuards.negation();
        Guard out = neg.getGuard(clocks);


        Log.trace(out);

                                    // ( ((x<=3)) or ((x>=7 && x<9)) )
        assert (out.toString().equals("(x<=3 or (x>=7 && x<9))"));


    }


}
