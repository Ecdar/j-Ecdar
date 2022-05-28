package connection;

import logic.Controller;
import logic.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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
        ArrayList<String> list =  new ArrayList<>(Arrays.asList(result.split("\n")));
        list.replaceAll(String::trim);
        list.removeIf(String::isEmpty);
        return list;
    }

    @Test
    public void testRunSingleQuery1() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:Spec<=Spec", false);

        assertEquals(false, queries.get(0).getResult());
        assertEquals("Duplicate process instance: Spec.\n", queries.get(0).getResultStrings());
    }

    @Test
    public void testRunSingleQuery2() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:(Administration||Machine||Researcher)<=Spec", false);

        assertEquals(true, queries.get(0).getResult());
    }

    @Test
    public void testRunSingleQuery3() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:(HalfAdm1&&HalfAdm2)<=Adm2", false);

        assertEquals(true, queries.get(0).getResult());
    }

    @Test
    public void testRunMultipleQueries() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:spec <= spec; refinement:Machine<=Machine", false);

        assertEquals(false, queries.get(0).getResult());
        assertEquals("Duplicate process instance: Spec.\n", queries.get(0).getResultStrings());

        assertEquals(false, queries.get(1).getResult());
        assertEquals("Duplicate process instance: Machine.\n", queries.get(1).getResultStrings());
    }

    @Test
    public void testRunMultipleQueries2() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:(Administration||Machine||Researcher)<=Spec; refinement:Machine3<=Machine3", false);

        assertEquals(true, queries.get(0).getResult());

        assertEquals(false, queries.get(1).getResult());
        assertEquals("Duplicate process instance: Machine3.\n", queries.get(1).getResultStrings());
    }

    @Test
    public void testRunMultipleQueries3() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:Spec<=(Administration||Machine||Researcher); refinement:Machine3<=Machine3", false);

        assertEquals(false, queries.get(0).getResult());
        assertEquals("Not all outputs of the right side are present on the left side.\n", queries.get(0).getResultStrings());

        assertEquals(false, queries.get(1).getResult());
        assertEquals("Duplicate process instance: Machine3.\n", queries.get(1).getResultStrings());
    }

    @Test
    public void testRunMultipleQueries4() throws Exception {
        List<Query> queries = Controller.handleRequest("-json " + "./samples/json/EcdarUniversity",
                "refinement:Spec<=Spec; refinement:Machine<=Machine; refinement:Machine3<=Machine3; refinement:Researcher<=Researcher", false);

        assertEquals(false, queries.get(0).getResult());
        assertEquals("Duplicate process instance: Spec.\n", queries.get(0).getResultStrings());

        assertEquals(false, queries.get(1).getResult());
        assertEquals("Duplicate process instance: Machine.\n", queries.get(1).getResultStrings());

        assertEquals(false, queries.get(2).getResult());
        assertEquals("Duplicate process instance: Machine3.\n", queries.get(2).getResultStrings());

        assertEquals(false, queries.get(3).getResult());
        assertEquals("Duplicate process instance: Researcher.\n", queries.get(3).getResultStrings());
    }

    @Test
    public void testRunMultipleQueries5() throws Exception {
        List<Query> queries = Controller.handleRequest("-xml " + "./samples/xml/ImplTests.xml",
                "refinement:G17<=G17; implementation:G14", false);

        assertEquals(false, queries.get(0).getResult());
        assertEquals("Duplicate process instance: G17.\n", queries.get(0).getResultStrings());

        assertEquals(false, queries.get(1).getResult());
        assertEquals("Automaton G14 is non-deterministic.\nAutomaton G14 is not output urgent.\n", queries.get(1).getResultStrings());
    }

    @Test(expected = FileNotFoundException.class)
    public void testRunInvalidQuery() throws Exception {
        List<Query> queries = Controller.handleRequest("-json sdfsd", "xcv", false);
    }

    @Test
    public void testRunInvalidQuery2() {
        String arg = "-machine 1 2 3";

        Main.main(arg.split(" "));
        assertEquals("Unrecognized option: -machine",getResult().get(0));
    }
}
