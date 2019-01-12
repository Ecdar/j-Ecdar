package connection;

import logic.Composition;
import logic.SimpleTransitionSystem;
import logic.TransitionSystem;
import main.Main;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    @Test
    public void testVersion() {
        assertEquals("Version 1.0", (Main.chooseCommand("-version")));
    }
    @Test
    public void testHelp() {
        assertEquals("In order to check version type:-version\n" +
                "In order to run query type:-rq folderPath query query...\n" +
                "In order to check the validity of a query type:-vq query", (Main.chooseCommand("-help")));
    }
    @Test
    public void testVerificationOfQuery() {
        System.out.println(Main.chooseCommand("-vq refinement:spec<=spec"));
        assertEquals("true", (Main.chooseCommand("-vq refinement:spec<=spec")));
    }
    @Test
    public void testRunSingleQuery() {
        assertEquals("true", (Main.chooseCommand("-rq ./samples/EcdarUniversity refinement:spec<=spec")));
    }
    @Test
    public void testRunMultipleQueries() {
        assertEquals("true true", (Main.chooseCommand("-rq ./samples/EcdarUniversity refinement:spec<=spec refinement:Machine<=Machine")));
    }
    @Test
    public void testIncorrectRunQuery() {
        assertEquals("Error: null", (Main.chooseCommand("-rq sdfsd xcv")));
    }
    @Test
    public void testIncorrectRunQuery2() {
        assertEquals("Server confirms having received: \"-machine 1 2 3\" try -help", (Main.chooseCommand("-machine 1 2 3")));
    }
    @Test
    public void testIncorrectValidationOfQuery3() {
        assertEquals("Error: Expected: \"refinement:\"", (Main.chooseCommand("-vq spec<=spec")));
    }
    @Test
    public void testIncorrectRunQuery3() {
        assertEquals("Error: Incorrect syntax, does not contain any feature", (Main.chooseCommand("-vq sdf")));
    }
}
