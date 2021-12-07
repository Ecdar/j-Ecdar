package models;

import logic.Quotient;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;
import parser.XMLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class DisjunctionTest {

    private static Automaton expected, actual;
    private static Update[] noUpdate = new Update[]{};
    private static List<List<Guard>> noguard = new ArrayList<>();


    static Automaton[] automata;
    private static TransitionSystem D1,D2;


    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/DisjunctionTests.xml", true);
        D1 = new SimpleTransitionSystem(automata[0]);
        D2 = new SimpleTransitionSystem(automata[1]);


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

        List<List<Guard>> disjunctedGuards = new ArrayList<>();
        List<Guard> disj1 = new ArrayList<>();
        disj1.add(g1);
        disj1.add(g2);
        List<Guard> disj2 = new ArrayList<>();
        disj2.add(g3);
        disj2.add(g4);
        List<Guard> disj3 = new ArrayList<>();
        disj3.add(g5);
        disj3.add(g6);
        disj3.add(g7);
        disjunctedGuards.add(disj1);
        disjunctedGuards.add(disj2);
        disjunctedGuards.add(disj3);

        List<List<Guard>> out = Quotient.negateGuards(disjunctedGuards);

        System.out.println(disjunctedGuards);
        System.out.println(out);



        assert(out.toString().equals("[[Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=6, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=8, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=7, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=4, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}], [Guard{clock=Clock{name='x'}, upperBound=2147483647, lowerBound=5, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=3, lowerBound=0, isStrict=false}, Guard{clock=Clock{name='x'}, upperBound=9, lowerBound=0, isStrict=false}]]"));


    }



}
