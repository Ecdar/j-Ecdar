package parser;

import logic.*;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class QueryParserTest {
    private static Controller ctrl;
    private static TransitionSystem adm, machine, researcher, spec, machine3, adm2, half1, half2;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/EcdarUniversity/";
        List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"));

        List<Automaton> machines = Parser.parse(base, components);
        adm = new SimpleTransitionSystem(machines.get(0));
        machine = new SimpleTransitionSystem(machines.get(1));
        researcher = new SimpleTransitionSystem(machines.get(2));
        spec = new SimpleTransitionSystem(machines.get(3));
        machine3 = new SimpleTransitionSystem(machines.get(4));
        adm2 = new SimpleTransitionSystem(machines.get(5));
        half1 = new SimpleTransitionSystem(machines.get(6));
        half2 = new SimpleTransitionSystem(machines.get(7));

        ctrl = new Controller();
        ctrl.parseComponents("./samples/EcdarUniversity");
    }

    @Test
    public void testCompositionOfThree() {
        TransitionSystem ts1 = new Composition(new ArrayList<>(Arrays.asList(adm, machine, researcher)));
        TransitionSystem ts2 = ctrl.runQuery("(Administration||Machine||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfOne() {
        TransitionSystem ts = ctrl.runQuery("(Spec)");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfOneMultiBrackets() {
        TransitionSystem ts = ctrl.runQuery("((Spec))");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfThreeExtraBrackets() {
        TransitionSystem transitionSystem1 = new Composition(new ArrayList<>(Arrays.asList(adm, machine)));
        ArrayList<TransitionSystem> trs2 = new ArrayList<>(Arrays.asList(transitionSystem1, researcher));

        TransitionSystem ts1 = new Composition(trs2);
        TransitionSystem ts2 = ctrl.runQuery("((Administration||Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThree() {
        TransitionSystem ts1 = new Conjunction(new ArrayList<>(Arrays.asList(adm, machine, researcher)));
        TransitionSystem ts2 = ctrl.runQuery("(Administration&&Machine&&Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThreeExtraBrackets() {
        TransitionSystem transitionSystem1 = new Conjunction(new ArrayList<>(Arrays.asList(adm, machine)));
        ArrayList<TransitionSystem> trs2 = new ArrayList<>(Arrays.asList(transitionSystem1, researcher));

        TransitionSystem ts1 = new Composition(trs2);
        TransitionSystem ts2 = ctrl.runQuery("((Administration&&Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery1() {
        TransitionSystem trs1 = new Conjunction(new ArrayList<>(Arrays.asList(adm, machine, machine)));

        TransitionSystem ts1 = new Composition(new ArrayList<>(Arrays.asList(trs1, researcher, half1)));
        TransitionSystem ts2 = ctrl.runQuery("((Administration&&Machine&&Machine)||Researcher||HalfAdm1)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery2() {
        TransitionSystem trs1 = new Conjunction(new ArrayList<>(Arrays.asList(machine, researcher)));
        ArrayList<TransitionSystem> ts0 = new ArrayList<>(Arrays.asList(researcher, machine, trs1, spec));

        TransitionSystem ts1 = new Composition(ts0);
        TransitionSystem ts2 = ctrl.runQuery("(Researcher||Machine||(Machine&&Researcher)||Spec)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery3() {
        TransitionSystem trs1 = new Conjunction(new ArrayList<>(Arrays.asList(researcher, machine)));
        TransitionSystem trs2 = new Conjunction(new ArrayList<>(Arrays.asList(machine, researcher)));

        TransitionSystem ts1 = new Composition(new ArrayList<>(Arrays.asList(trs1, trs2)));
        TransitionSystem ts2 = ctrl.runQuery("((Researcher&&Machine)||(Machine&&Researcher))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery4() {
        TransitionSystem trs1 = new Composition(new ArrayList<>(Arrays.asList(machine, researcher)));
        ArrayList<TransitionSystem> tss = new ArrayList<>(Arrays.asList(spec, trs1, machine));
        TransitionSystem trs2 = new Conjunction(tss);
        TransitionSystem trs3 = new Conjunction(new ArrayList<>(Arrays.asList(machine, machine, machine)));

        TransitionSystem ts1 = new Composition(new ArrayList<>(Arrays.asList(researcher, trs3, trs2)));
        TransitionSystem ts2 = ctrl.runQuery("(Researcher||(Machine&&Machine&&Machine)||(Spec&&(Machine||Researcher)&&Machine))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void Half1ConjHalf2() {
        TransitionSystem ts1 = new Conjunction(new ArrayList<>(Arrays.asList(half1, half2)));
        TransitionSystem ts2 = ctrl.runQuery("(HalfAdm1&&HalfAdm2)");
        assertEquals(ts1, ts2);
    }


    //Test entire Controller component

    @Test
    public void testCompRefinesSpec() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecRefinesSpec() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:(Spec)<=(Spec)");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMachRefinesMach() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Machine<=Machine");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMach3RefinesMach3() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Machine3<=Machine3");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMach3RefinesMach() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Machine3<=Machine");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesAdm() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Spec<=Administration");
            assertFalse(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesMachine() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Spec<=Machine");
            assertFalse(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Spec<=Researcher");
            assertFalse(result.get(0));
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testSpecNotRefinesMachine3() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Spec<=Machine3");
            assertFalse(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCompRefinesComp() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:(Administration||Machine||Researcher)<=(Administration||Machine||Researcher)");
            assertTrue(result.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testConjRefinesAdm2() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:(HalfAdm1&&HalfAdm2)<=Adm2");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testAdm2RefinesConj() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
            assertTrue(result.get(0));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSeveralQueries1() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:Spec<=Spec refinement:Machine<=Machine");
            assertTrue(result.get(0) && result.get(1));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSeveralQueries2() {
        try {
            List<Boolean> result = ctrl.handleRequest("./samples/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec refinement:Machine3<=Machine3");
            assertTrue(result.get(0) && result.get(1));
        } catch (Exception e) {
            fail();
        }
    }

    //Query validator tests

    @Test
    public void testQueryValid1() {
        try {
            ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid2() {
        try {
            ctrl.isQueryValid("refinement:(Administration||Researcher||Machine)<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid3() {
        try {
            ctrl.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||Machine)<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testQueryValid4() {
        try {
            ctrl.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid5() {
        try {
            ctrl.isQueryValid("refinement:((HalfAdm1&&(HA1||HA2))||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryNotValid1() {
        try {
            ctrl.isQueryValid("refinsdfement:Adm2<=(HalfAdm1&&HalfAdm2)");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Expected: \"refinement:\"");
        }
    }

    @Test
    public void testQueryNotValid2() {
        try {
            ctrl.isQueryValid("refinement:Adm2(<=(HalfAdm1&&HalfAdm2)");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Parentheses are not balanced");
        }
    }

    @Test
    public void testQueryNotValid3() {
        try {
            ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)<=Spec");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "There can only be one refinement");
        }
    }

    @Test
    public void testQueryNotValid4() {
        try {
            ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1(&&HalfAdm2))");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Before opening Parentheses can be either operator or second Parentheses");
        }
    }

    @Test
    public void testQueryNotValid5() {
        try {
            ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1||(&&HalfAdm2))");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "After opening Parentheses can be either other Parentheses or component");
        }
    }
}
