package connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    public ArrayList<String> getResult(){
        String result = outContent.toString();
        result = result.substring(result.lastIndexOf("[") + 1);
        result = result.substring(0, result.lastIndexOf("]"));
        ArrayList<String> list =  new ArrayList<>(Arrays.asList(result.split(",")));
        list.replaceAll(String::trim);
        list.removeIf(String::isEmpty);
        return list;
    }

    @Test
    public void testRunSingleQuery1() {
        String arg = "-i samples/json/EcdarUniversity refinement:Spec<=Spec";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("false", "Duplicate process instance: Spec."), getResult());
    }

    @Test
    public void testRunSingleQuery2() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("true"), getResult());
    }

    @Test
    public void testRunSingleQuery3() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:(HalfAdm1&&HalfAdm2)<=Adm2";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("true"), getResult());
    }

    @Test
    public void testRunMultipleQueries() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:spec <= spec; refinement:Machine<=Machine";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("false","Duplicate process instance: Spec.","false","Duplicate process instance: Machine."), getResult());
    }

    @Test
    public void testRunMultipleQueries2() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec; refinement:Machine3<=Machine3";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("true","false","Duplicate process instance: Machine3."), getResult());
    }

    @Test
    public void testRunMultipleQueries3() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:Spec<=(Administration||Machine||Researcher); refinement:Machine3<=Machine3";
        Main.main(arg.split(" "));
        ArrayList<String> res = getResult();
        assertEquals(Arrays.asList("false","Not all outputs of the right side are present on the left side.","false","Duplicate process instance: Machine3."), res);
    }

    @Test
    public void testRunMultipleQueries4() {
        String arg = "-i ./samples/json/EcdarUniversity refinement:Spec<=Spec; refinement:Machine<=Machine; refinement:Machine3<=Machine3; refinement:Researcher<=Researcher";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("false","Duplicate process instance: Spec.","false","Duplicate process instance: Machine.","false",
                "Duplicate process instance: Machine3.","false","Duplicate process instance: Researcher."), getResult());
    }

    @Test
    public void testRunMultipleQueries5() {
        String arg = "-i ./samples/xml/ImplTests.xml refinement:G17<=G17; implementation:G14";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("false","Duplicate process instance: G17.","false","Automaton G14 is non-deterministic.","Automaton G14 is not output urgent."), getResult());
    }

    @Test
    public void testRunInvalidQuery() {
        String arg = "-i sdfsd xcv";
        Main.main(arg.split(" "));
        assertEquals(Arrays.asList("Error: null"), getResult());
    }

    @Test
    public void testRunInvalidQuery2() {
        String arg = "-machine 1 2 3";
        //List<String> expected = Arrays.asList("Unknown command:","-machine 1 2 3","write -help to get list of commands");
        List<String> expected = Arrays.asList("\"QUERIES\"");
        Main.main(arg.split(" "));
        assertEquals(expected,getResult());
    }
}
