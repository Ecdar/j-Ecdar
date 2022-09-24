package e2e;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsistencyTest extends GrpcE2EBase {
    public ConsistencyTest() {
        super("./samples/xml/ConsTests.xml");
    }

    @Test
    @Ignore
    public void g1IsConsistent() {
        System.out.println("g1IsConsistent");
        assertTrue(consistency("consistency: G1"));
    }

    @Test
    @Ignore
    public void g2IsConsistent() {
        System.out.println("g2IsConsistent");
        assertTrue(consistency("consistency: G2"));
    }

    @Test
    @Ignore
    public void g3IsNotConsistent() {
        System.out.println("g3IsNotConsistent");
        assertFalse(consistency("consistency: G3"));

    }

    @Test
    @Ignore
    public void g4IsNotConsistent() {
        System.out.println("g4IsNotConsistent");
        assertFalse(consistency("consistency: G4"));
    }

    @Test
    @Ignore
    public void g5IsNotConsistent() {
        System.out.println("g5IsNotConsistent");
        assertFalse(consistency("consistency: G5"));
    }

    @Test
    @Ignore
    public void g6IsConsistent() {
        System.out.println("g6IsConsistent");
        assertTrue(consistency("consistency: G6"));
    }

    @Test
    @Ignore
    public void g7IsNotConsistent() {
        System.out.println("g7IsNotConsistent");
        assertFalse(consistency("consistency: G7"));
    }

    @Test
    @Ignore
    public void g8IsConsistent() {
        System.out.println("g8IsConsistent");
        assertTrue(consistency("consistency: G8"));
    }

    @Test
    @Ignore
    public void g9IsNotConsistent() {
        System.out.println("g9IsNotConsistent");
        assertFalse(consistency("consistency: G9"));
    }

    @Test
    @Ignore
    public void g10IsNotConsistent() {
        System.out.println("g10IsNotConsistent");
        assertFalse(consistency("consistency: G10"));
    }

    @Test
    @Ignore
    public void g11IsNotConsistent() {
        System.out.println("g11IsNotConsistent");
        assertFalse(consistency("consistency: G11"));
    }

    @Test
    @Ignore
    public void g12IsNotConsistent() {
        System.out.println("g12IsNotConsistent");
        assertFalse(consistency("consistency: G12"));
    }

    @Test
    @Ignore
    public void g13IsConsistent() {
        System.out.println("g13IsConsistent");
        assertTrue(consistency("consistency: G13"));
    }

    @Test
    @Ignore
    public void g14IsNotConsistent() {
        System.out.println("g14IsNotConsistent");
        assertFalse(consistency("consistency: G14"));
    }

    @Test
    @Ignore
    public void g15IsConsistent() {
        System.out.println("g15IsConsistent");
        assertTrue(consistency("consistency: G15"));
    }

    @Test
    @Ignore // Causes non-deterministically problems with "cdd_tarjan_reduce_rec"
    public void g16IsNotConsistent() {
        System.out.println("g16IsNotConsistent");
        assertFalse(consistency("consistency: G16"));
    }

    @Test
    public void g17IsConsistent() {
        System.out.println("g17IsConsistent");
        assertTrue(consistency("consistency: G17"));
    }

    @Test
    @Ignore
    public void g18IsConsistent() {
        System.out.println("g18IsConsistent");
        assertTrue(consistency("consistency: G18"));
    }

    @Test
    public void g19IsNotConsistent() {
        System.out.println("g19IsNotConsistent");
        assertFalse(consistency("consistency: G19"));
    }

    @Test
    public void g20IsConsistent() {
        System.out.println("g20IsConsistent");
        assertTrue(consistency("consistency: G20"));
    }

    @Test
    public void g21IsConsistent() {
        System.out.println("g21IsConsistent");
        assertTrue(consistency("consistency: G21"));
    }

    @Test
    @Ignore
    public void g22IsConsistent() {
        System.out.println("g22IsConsistent");
        assertTrue(consistency("consistency: G22"));
    }

    @Test
    @Ignore
    public void g23IsNotConsistent() {
        System.out.println("g23IsNotConsistent");
        assertFalse(consistency("consistency: G23"));
    }
}
