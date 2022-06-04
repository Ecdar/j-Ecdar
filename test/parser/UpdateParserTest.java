package parser;

import models.BoolVar;
import models.Clock;
import models.ClockUpdate;
import models.Update;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UpdateParserTest {
    @Test
    public void testUpdateParser(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("z")); }};

        List<Update> updates = UpdateParser.parse("z=2", clocks, BVs);

        assertEquals(2, ((ClockUpdate)updates.get(0)).getValue());
        assertEquals("z", ((ClockUpdate)updates.get(0)).getClock().getName());
    }

    @Test
    public void testUpdateParserMultiple(){
        List<BoolVar> BVs = new ArrayList<>();
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};

        List<Update> updates = UpdateParser.parse("x=6, y=1", clocks, BVs);

        assertEquals(6, ((ClockUpdate)updates.get(0)).getValue());
        assertEquals("x", ((ClockUpdate)updates.get(0)).getClock().getName());
        assertEquals(1, ((ClockUpdate)updates.get(1)).getValue());
        assertEquals("y", ((ClockUpdate)updates.get(1)).getClock().getName());
    }
}
