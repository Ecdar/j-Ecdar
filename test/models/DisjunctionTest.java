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

        automata = XMLParser.parse("D1_test.xml", false);
        D1 = new SimpleTransitionSystem(automata[0]);
        automata = XMLParser.parse("D2_test.xml", false);
        D2 = new SimpleTransitionSystem(automata[0]);

        assert(new Refinement(D1, D2).check() && new Refinement(D2, D1).check());


    }


    @Test
    public void testGuardNegation() {
        Clock x = new Clock("x");
        Guard g1 = new Guard(x, 4,true,true);
        Guard g2 = new Guard(x, 5,false,true);
        Guard g3 = new Guard(x, 7,false,true);
        Guard g4 = new Guard(x, 3,true,true);
        Guard g5 = new Guard(x, 6,true,true);
        Guard g6 = new Guard(x, 8,true,true);
        Guard g7 = new Guard(x, 9,true,true);

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
        CDD disjunctedGuards = CDD.cddFalse();
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis1));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis2));
        disjunctedGuards = disjunctedGuards.disjunction(new CDD(dis3));

        List<List<Guard>> out = CDD.toGuards(disjunctedGuards.negation());

        System.out.println(disjunctedGuards);
        System.out.println(out);


        assert(out.toString().equals("[[Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}]]"));


    }



}
