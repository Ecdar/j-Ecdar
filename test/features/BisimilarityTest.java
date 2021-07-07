package features;

import logic.Bisimilarity;
import logic.Composition;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import org.junit.Test;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class BisimilarityTest {

    @Test
    public void bisimilarityTest1() {



        Automaton[] auts = XMLParser.parse("./samples/xml/bisimilarity.xml", true);
        Automaton aut  = Bisimilarity.checkBisimilarity(auts[0]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        sys.toXML("bisim2.xml");

        assertTrue(true);
    }

    @Test
    public void bisimilarityUntimedTest() {



        Automaton[] auts = XMLParser.parse("./samples/xml/quotient/example_critical_sections_final_versions_pruned.xml", false);
        Automaton aut = Bisimilarity.checkBisimilarity(auts[9]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        sys.toXML("bisim1.xml");
        assertTrue(aut.getLocations().size()==2);
    }
    @Test
    public void bisimilarityUntimedTestNotBisim() {



        Automaton[] auts = XMLParser.parse("./samples/xml/quotient/example_critical_sections_final_versions_pruned.xml", false);
        Automaton aut = Bisimilarity.checkBisimilarity(auts[10]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        sys.toXML("bisim3.xml");
        assertTrue(aut.getLocations().size()==3);
    }

}
