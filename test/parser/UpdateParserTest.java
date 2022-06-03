package parser;

import models.Clock;
import models.Update;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UpdateParserTest {
    @Test
    public void testUpdateParser(){
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("z")); }};

        List<Update> updates = UpdateParser.parse("z=2", clocks);

        assertEquals(2, updates.get(0).getValue());
        assertEquals("z", updates.get(0).getClock().getName());
    }

    @Test
    public void testUpdateParserMultiple(){
        ArrayList<Clock> clocks = new ArrayList<Clock>() {{add(new Clock("x")); add(new Clock("y")); }};

        List<Update> updates = UpdateParser.parse("x=6, y=1", clocks);

        assertEquals(6, updates.get(0).getValue());
        assertEquals("x", updates.get(0).getClock().getName());
        assertEquals(1, updates.get(1).getValue());
        assertEquals("y", updates.get(1).getClock().getName());
    }
}
