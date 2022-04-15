package features;

import logic.Composition;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import models.Automaton;
import models.CDD;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.XMLParser;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ImplementationTest {

    static Automaton[] automata;
    private static TransitionSystem G7, G8, G13;

    @After
    public void afterEachTest(){
        CDD.done();
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        automata = XMLParser.parse("./samples/xml/ImplTests.xml", true);

        G7 = new SimpleTransitionSystem(automata[7]);
        G8 = new SimpleTransitionSystem(automata[8]);
        G13 = new SimpleTransitionSystem(automata[13]);
    }

    @Test
    public void testG1(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[1].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[1]));

        
        assertFalse(ts.isImplementation());
    }
    @Test
    public void testG2(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[2].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[2]));

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG3(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[3].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[3]));

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG4(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[4].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[4]));

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG5(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[5].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[5]));

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void G8G13IsImplementation(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[8].getClocks(),automata[13].getClocks() );
        TransitionSystem ts = new Composition(new TransitionSystem[]{G8, G13});

        
        assertTrue(ts.isImplementation());
    }

    @Test
    public void G7G13IsNotImplementation(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[7].getClocks(), automata[13].getClocks());
        TransitionSystem ts = new Composition(new TransitionSystem[]{G7, G13});

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG6(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[6].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[6]);
        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG7(){

        CDD.init(100,100,100);
        CDD.addClocks(automata[7].getClocks() );
        assertFalse(G7.isImplementation());
    }

    @Test
    public void testG8(){

        CDD.init(100,100,100);
        CDD.addClocks(automata[8].getClocks() );
        assertTrue(G8.isImplementation());
    }

    @Test
    public void testG9(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[9].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[9]);


        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG10(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[10].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[10]);

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG11(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[11].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[11]);

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG12(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[12].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(CDD.makeInputEnabled(automata[12]));
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG13(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[13].getClocks() );
        boolean res = G13.isImplementation();
        System.out.println(G13.getLastErr());
        assertTrue(res);
    }

    @Test
    public void testG14(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[14].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[14]);

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG15(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[15].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[15]);

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG16(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[16].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[16]);

        
        assertFalse(ts.isImplementation());
    }

    @Test
    public void testG17(){
        CDD.init(100,100,100);
        CDD.addClocks(automata[17].getClocks() );
        TransitionSystem ts = new SimpleTransitionSystem(automata[17]);

        
        assertTrue(ts.isImplementation());
    }
}
