package features;

import log.Log;
import logic.Bisimilarity;
import logic.SimpleTransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.Test;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;

public class BisimilarityTest {

    @After
    public void afterEachTest(){
    }

    @Test
    public void bisimilarityTest1() {



        Automaton[] auts = XMLParser.parse("./samples/xml/bisimilarity.xml", true);
        Automaton aut  = Bisimilarity.checkBisimilarity(auts[0]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        sys.toXML("testOutput/bisim2.xml");

        assertTrue(true);
    }

    @Test
    public void bisimilarityUntimedTest() {



        Automaton[] auts = XMLParser.parse("./samples/xml/quotient/example_critical_sections_final_versions_pruned.xml", false);
        Automaton aut = Bisimilarity.checkBisimilarity(auts[9]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        sys.toXML("testOutput/bisim1.xml");
        assertTrue(aut.getLocations().size()==2);
    }
    @Test
    public void bisimilarityUntimedTestNotBisim() {

        CDD.done();

        Automaton[] auts = XMLParser.parse("./samples/xml/quotient/example_critical_sections_final_versions_pruned.xml", false);
        Automaton aut = Bisimilarity.checkBisimilarity(auts[10]);
        SimpleTransitionSystem sys = new SimpleTransitionSystem(aut);
        // sys.toXML("testOutput/bisim3.xml");
        Log.trace(aut.getLocations().size());
        assertTrue(aut.getLocations().size()==3);
    }

}
