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
    private static List<List<Expression>> noguard = new ArrayList<>();


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
        Expression g1 = new ClockExpression(x, null, 4, Relation.GREATER_THAN);  //x>4
        Expression g2 = new ClockExpression(x, null, 5, Relation.LESS_THAN); //x<5
        Expression g3 = new ClockExpression(x, null, 7, Relation.LESS_THAN); //x<7
        Expression g4 = new ClockExpression(x, null, 3, Relation.GREATER_THAN); //x>3
        Expression g5 = new ClockExpression(x, null, 6, Relation.GREATER_THAN); //x>6
        Expression g6 = new ClockExpression(x, null, 8, Relation.GREATER_THAN); //x>8
        Expression g7 = new ClockExpression(x, null, 9, Relation.GREATER_EQUAL); //x>9

        List<Expression> disj1 = new ArrayList<>();
        disj1.add(g1);
        disj1.add(g2);
        Expression dis1 = new AndExpression(disj1);
        List<Expression> disj2 = new ArrayList<>();
        disj2.add(g3);
        disj2.add(g4);
        Expression dis2 = new AndExpression(disj2);
        List<Expression> disj3 = new ArrayList<>();
        disj3.add(g5);
        disj3.add(g6);
        disj3.add(g7);
        Expression dis3 = new AndExpression(disj3);


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
        Expression out = neg.getExpression(clocks);


        Log.trace(out);

                                    // ( ((x<=3)) or ((x>=7 && x<9)) )
        assert (out.toString().equals("(x<=3 or (x>=7 && x<9))"));


    }


}
