package parser;

import models.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class XMLParserTest {

    private static Automaton expected, actual;

    @BeforeClass
    public static void setUpBeforeClass() {
        actual = XMLParser.parse("./samples/xml/ImplTests.xml")[0];

        Clock x = new Clock("x");
        Clock y = new Clock("y");
        Clock z = new Clock("z");
        Clock[] clocks = new Clock[]{x, y, z};

        Channel i = new Channel("i");
        Channel o = new Channel("o");

        Guard inv0_0 = new Guard(y, 50, false, false);
        Guard inv0_1 = new Guard(z, 40, false, false);

        Location l0 = new Location("id0", new Guard[]{inv0_0, inv0_1}, false, false, false, false);
        Location l1 = new Location("id1", new Guard[]{}, false, false, false, false);
        Location l2 = new Location("id2", new Guard[]{}, true, false, false, false);
        Location[] locations = new Location[]{l0, l1, l2};

        Guard g2 = new Guard(x, 4);
        Update u2_0 = new Update(x, 0);
        Update u2_1 = new Update(y, 0);
        Update u2_2 = new Update(z, 0);
        Edge e0 = new Edge(l1, l2, i, true, new Guard[]{}, new Update[]{});
        Edge e1 = new Edge(l2, l0, o, false, new Guard[]{}, new Update[]{});
        Edge e2 = new Edge(l2, l1, i, true, new Guard[]{g2}, new Update[]{u2_0, u2_1, u2_2});
        Edge[] edges = new Edge[]{e0, e1, e2};

        expected = new Automaton("Test", locations, edges, clocks);
    }

    @Test
    public void testAutomaton() {
        assert expected.equals(actual);
    }
}
