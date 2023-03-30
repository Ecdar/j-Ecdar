package e2e;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsistencyTest extends GrpcE2EBase {
    public ConsistencyTest() {
        super("./samples/xml/ConsTests.xml");
    }

    @Test
    public void g1IsConsistent() {
        assertTrue(consistency("consistency: G1"));
    }

    @Test
    public void g2IsConsistent() {
        assertTrue(consistency("consistency: G2"));
    }

    @Test
    public void g3IsNotConsistent() {
        assertFalse(consistency("consistency: G3"));
    }

    @Test
    public void g4IsNotConsistent() {
        assertFalse(consistency("consistency: G4"));
    }

    @Test
    public void g5IsNotConsistent() {
        assertFalse(consistency("consistency: G5"));
    }

    @Test
    public void g6IsConsistent() {
        assertTrue(consistency("consistency: G6"));
    }

    @Test
    public void g7IsNotConsistent() {
        assertFalse(consistency("consistency: G7"));
    }

    @Test
    public void g8IsConsistent() {
        assertTrue(consistency("consistency: G8"));
    }

    @Test
    public void g9IsNotConsistent() {
        assertFalse(consistency("consistency: G9"));
    }

    @Test
    public void g10IsNotConsistent() {
        assertFalse(consistency("consistency: G10"));
    }

    @Test
    public void g11IsNotConsistent() {
        assertFalse(consistency("consistency: G11"));
    }

    @Test
    public void g12IsNotConsistent() {
        assertFalse(consistency("consistency: G12"));
    }

    @Test
    public void g13IsConsistent() {
        assertTrue(consistency("consistency: G13"));
    }

    @Test
    public void g14IsNotConsistent() {
        assertFalse(consistency("consistency: G14"));
    }

    @Test
    public void g15IsConsistent() {
        assertTrue(consistency("consistency: G15"));
    }

    @Test
    public void g16IsNotConsistent() {
        assertFalse(consistency("consistency: G16"));
    }

    @Test
    public void g17IsConsistent() {
        assertTrue(consistency("consistency: G17"));
    }

    @Test
    public void g18IsConsistent() {
        assertTrue(consistency("consistency: G18"));
    }

    @Test
    public void g19IsNotConsistent() {
        assertFalse(consistency("consistency: G19"));
    }

    @Test
    public void g20IsConsistent() {
        assertTrue(consistency("consistency: G20"));
    }

    @Test
    public void g21IsConsistent() {
        assertTrue(consistency("consistency: G21"));
    }

    @Test
    public void g22IsConsistent() {
        assertTrue(consistency("consistency: G22"));
    }

    @Test
    public void g23IsNotConsistent() {
        assertFalse(consistency("consistency: G23"));
    }
}
