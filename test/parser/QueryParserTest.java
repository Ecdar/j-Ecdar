package parser;

import logic.*;
import models.Channel;
import models.Component;
import models.Location;
import models.Transition;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class QueryParserTest {
    private static Controller ctrl;
    private static ArrayList<Component> components;
    private static Component adm, machine, researcher, spec, machine3, adm2, half1, half2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
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
        List<Component> machines = Parser.parse(base, components);
        adm = machines.get(0);
        machine = machines.get(1);
        researcher = machines.get(2);
        spec = machines.get(3);
        machine3 = machines.get(4);
        adm2 = machines.get(5);
        half1 = machines.get(6);
        half2 = machines.get(7);
        ctrl = new Controller();
        ctrl.parseComponents("./samples/EcdarUniversity");
    }

    @Test
    public void testCompositionOfThree() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(researcher));
        TransitionSystem ts1 = new Composition(ts);
        TransitionSystem ts2 = ctrl.runQuery("(Administration||Machine||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfOne() {
        SimpleTransitionSystem ts1 = new SimpleTransitionSystem(spec);
        TransitionSystem ts2 = ctrl.runQuery("(Spec)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfOneMultiBrackets() {
        SimpleTransitionSystem ts1 = new SimpleTransitionSystem(spec);
        TransitionSystem ts2 = ctrl.runQuery("((Spec))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfThreeExtraBrackets() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem transitionSystem1 = new Composition(ts);
        ArrayList<TransitionSystem> trs2 = new ArrayList<>();
        trs2.add(transitionSystem1);
        trs2.add(new SimpleTransitionSystem(researcher));

        TransitionSystem ts1 = new Composition(trs2);
        TransitionSystem ts2 = ctrl.runQuery("((Administration||Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThree() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(researcher));

        TransitionSystem ts1 = new Conjunction(ts);
        TransitionSystem ts2 = ctrl.runQuery("(Administration&&Machine&&Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThreeExtraBrackets() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem transitionSystem1 = new Conjunction(ts);
        ArrayList<TransitionSystem> trs2 = new ArrayList<>();
        trs2.add(transitionSystem1);
        trs2.add(new SimpleTransitionSystem(researcher));

        TransitionSystem ts1 = new Composition(trs2);
        TransitionSystem ts2 = ctrl.runQuery("((Administration&&Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem trs1 = new Conjunction(ts);
        ArrayList<TransitionSystem> trs2 = new ArrayList<>();
        trs2.add(trs1);
        trs2.add(new SimpleTransitionSystem(researcher));
        trs2.add(new SimpleTransitionSystem(half1));

        TransitionSystem ts1 = new Composition(trs2);
        TransitionSystem ts2 = ctrl.runQuery("((Administration&&Machine&&Machine)||Researcher||HalfAdm1)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery3() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(researcher));
        ts0.add(new SimpleTransitionSystem(machine));


        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(researcher));
        TransitionSystem trs1 = new Conjunction(ts);
        ts0.add(trs1);
        ts0.add(new SimpleTransitionSystem(spec));

        TransitionSystem ts1 = new Composition(ts0);
        TransitionSystem ts2 = ctrl.runQuery("(Researcher||Machine||(Machine&&Researcher)||Spec)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery4() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(researcher));
        ts0.add(new SimpleTransitionSystem(machine));
        TransitionSystem trs1 = new Conjunction(ts0);

        ArrayList<TransitionSystem> trs2 = new ArrayList<>();
        trs2.add(new SimpleTransitionSystem(machine));
        trs2.add(new SimpleTransitionSystem(researcher));
        TransitionSystem trs3 = new Conjunction(trs2);
        ArrayList<TransitionSystem> tss = new ArrayList<>();
        tss.add(trs1);
        tss.add(trs3);

        TransitionSystem ts1 = new Composition(tss);
        TransitionSystem ts2 = ctrl.runQuery("((Researcher&&Machine)||(Machine&&Researcher))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery5() {
        ArrayList<TransitionSystem> trs0 = new ArrayList<>();
        trs0.add(new SimpleTransitionSystem(researcher));


        ArrayList<TransitionSystem> trs2 = new ArrayList<>();
        trs2.add(new SimpleTransitionSystem(machine));
        trs2.add(new SimpleTransitionSystem(machine));
        trs2.add(new SimpleTransitionSystem(machine));
        TransitionSystem trs3 = new Conjunction(trs2);
        trs0.add(trs3);

        ArrayList<TransitionSystem> tss = new ArrayList<>();
        tss.add(new SimpleTransitionSystem(spec));


        ArrayList<TransitionSystem> ts5 = new ArrayList<>();
        ts5.add(new SimpleTransitionSystem(machine));
        ts5.add(new SimpleTransitionSystem(researcher));
        TransitionSystem ts6 = new Composition(ts5);

        tss.add(ts6);
        tss.add(new SimpleTransitionSystem(machine));
        TransitionSystem ts7 = new Conjunction(tss);
        trs0.add(ts7);


        TransitionSystem ts1 = new Composition(trs0);
        TransitionSystem ts2 = ctrl.runQuery("(Researcher||(Machine&&Machine&&Machine)||(Spec&&(Machine||Researcher)&&Machine))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void Half1ConjHalf2() {
        ArrayList<TransitionSystem> trs0 = new ArrayList<>();
        trs0.add(new SimpleTransitionSystem(half1));
        trs0.add(new SimpleTransitionSystem(half2));

        TransitionSystem ts1 = new Conjunction(trs0);
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
    public void testQueryValid2() {
        try {
            boolean result = ctrl.isQueryValid("refinement:(Administration||Researcher||Machine)<=Spec");
            assertTrue(result);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid3() {
        try {
            boolean result = ctrl.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||Machine)<=Spec");
            assertTrue(result);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testQueryValid4() {
        try {
            boolean result = ctrl.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(result);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid5() {
        try {
            boolean result = ctrl.isQueryValid("refinement:((HalfAdm1&&(HA1||HA2))||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(result);
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
    public void testQueryValid1() {
        try {
            boolean result = ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
            assertTrue(result);
        } catch (Exception e) {
            fail();
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
