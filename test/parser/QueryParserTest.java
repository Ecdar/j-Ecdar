package parser;

import logic.*;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QueryParserTest {
    private static TransitionSystem adm, machine, researcher, spec, machine3, adm2, half1, half2;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/EcdarUniversity/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"};

        Automaton[] machines = JSONParser.parse(base, components, true);
        adm = new SimpleTransitionSystem(machines[0]);
        machine = new SimpleTransitionSystem(machines[1]);
        researcher = new SimpleTransitionSystem(machines[2]);
        spec = new SimpleTransitionSystem(machines[3]);
        machine3 = new SimpleTransitionSystem(machines[4]);
        adm2 = new SimpleTransitionSystem(machines[5]);
        half1 = new SimpleTransitionSystem(machines[6]);
        half2 = new SimpleTransitionSystem(machines[7]);

        Controller.parseComponents(base, true);
    }

    @Test
    public void testCompositionOfThree() {
        TransitionSystem ts1 = new Composition(new TransitionSystem[]{adm, machine, researcher});
        TransitionSystem ts2 = Controller.runQuery("(Administration||Machine||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfOne() {
        TransitionSystem ts = Controller.runQuery("(Spec)");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfOneMultiBrackets() {
        TransitionSystem ts = Controller.runQuery("((Spec))");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfThreeExtraBrackets() {
        TransitionSystem transitionSystem1 = new Composition(new TransitionSystem[]{adm, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{transitionSystem1, researcher});
        TransitionSystem ts2 = Controller.runQuery("((Administration||Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThree() {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[]{adm, machine, researcher});
        TransitionSystem ts2 = Controller.runQuery("(Administration&&Machine&&Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThreeExtraBrackets() {
        TransitionSystem transitionSystem1 = new Conjunction(new TransitionSystem[]{adm, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{transitionSystem1, researcher});
        TransitionSystem ts2 = Controller.runQuery("((Administration&&Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery1() {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{adm, machine, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{trs1, researcher, half1});
        TransitionSystem ts2 = Controller.runQuery("((Administration&&Machine&&Machine)||Researcher||HalfAdm1)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery2() {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{machine, researcher});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{researcher, machine, trs1, spec});
        TransitionSystem ts2 = Controller.runQuery("(Researcher||Machine||(Machine&&Researcher)||Spec)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery3() {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{researcher, machine});
        TransitionSystem trs2 = new Conjunction(new TransitionSystem[]{machine, researcher});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{trs1, trs2});
        TransitionSystem ts2 = Controller.runQuery("((Researcher&&Machine)||(Machine&&Researcher))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery4() {
        TransitionSystem trs1 = new Composition(new TransitionSystem[]{machine, researcher});
        TransitionSystem trs2 = new Conjunction(new TransitionSystem[]{spec, trs1, machine});
        TransitionSystem trs3 = new Conjunction(new TransitionSystem[]{machine, machine, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{researcher, trs3, trs2});
        TransitionSystem ts2 = Controller.runQuery("(Researcher||(Machine&&Machine&&Machine)||(Spec&&(Machine||Researcher)&&Machine))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void Half1ConjHalf2() {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[]{half1, half2});
        TransitionSystem ts2 = Controller.runQuery("(HalfAdm1&&HalfAdm2)");
        assertEquals(ts1, ts2);
    }


    //Test entire Controller component

    @Test
    public void testCompRefinesSpec() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesSpec() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:(Spec)<=(Spec)", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMachNotRefinesMach() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Machine<=Machine", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMach3NotRefinesMach3() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Machine3<=Machine3", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMach3RefinesMach() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Machine3<=Machine", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesAdm() {
        try {
            assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Spec<=Administration", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesMachine() {
        try {
            assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Spec<=Machine", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        try {
            assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Spec<=Researcher", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testSpecNotRefinesMachine3() {
        try {
            assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Spec<=Machine3", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCompNotRefinesComp() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:(Administration||Machine||Researcher)<=(Administration||Machine||Researcher)", false).get(0), "false");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testConjRefinesAdm2() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:(HalfAdm1&&HalfAdm2)<=Adm2", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermOne() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity determinism:HalfAdm1", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermConj() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity determinism:HalfAdm1&&HalfAdm2", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermComp() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity determinism:Administration||Machine", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermCompConj() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity determinism:Administration||Machine&&Researcher", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermOneFail() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml determinism:G9", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermCompFail() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml determinism:G9||G10", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermConjFail() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml determinism:G9&&G10", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testDetermConjCompFail() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml determinism:G9&&G10||G11", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testConsistencyOne() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity consistency:HalfAdm1", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testConsistencyConj() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity consistency:HalfAdm1&&HalfAdm2", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testConsistencyComp() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity consistency:Administration||Machine", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testConsistencyCompConj() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity consistency:Administration||Machine&&Researcher", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testImplementationComp() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml implementation:G8||G13", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testFailImplementationComp() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml implementation:G8||G15", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testImplementationOne() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml implementation:G8", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }
    @Test
    public void testFailImplementationOne() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml implementation:G9", false).get(0), "false");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testAdm2RefinesConj() {
        try {
            assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Adm2<=(HalfAdm1&&HalfAdm2)", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSeveralQueries1() {
        try {
            List<String> result = Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:Spec<=Spec refinement:Machine<=Machine", false);
            assertTrue(result.get(0).equals("false") && result.get(3).equals("false"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSeveralQueries2() {
        try {
            List<String> result = Controller.handleRequest("-json ./samples/json/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec refinement:Machine3<=Machine3", false);
            assertTrue(result.get(0).equals("true") && result.get(2).equals("false"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDelayRefZ3RefinesZ4() {
        try {
            assertEquals(Controller.handleRequest("-xml ./samples/xml/delayRefinement.xml refinement:Z3<=Z4", false).get(0), "true");
        } catch (Exception e) {
            fail();
        }
    }

    //Query validator tests

    @Test
    public void testQueryValid1() {
        try {
            Controller.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid2() {
        try {
            Controller.isQueryValid("refinement:(Administration||Researcher||Machine)<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid3() {
        try {
            Controller.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||Machine)<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testQueryValid4() {
        try {
            Controller.isQueryValid("refinement:((HalfAdm1&&HalfAdm2)||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryValid5() {
        try {
            Controller.isQueryValid("refinement:((HalfAdm1&&(HA1||HA2))||Researcher||(Machine1&&Machine2))<=Spec");
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testQueryNotValid1() {
        try {
            Controller.isQueryValid("refinsdfement:Adm2<=(HalfAdm1&&HalfAdm2)");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Expected: \"refinement:\"");
        }
    }

    @Test
    public void testQueryNotValid2() {
        try {
            Controller.isQueryValid("refinement:Adm2(<=(HalfAdm1&&HalfAdm2)");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Parentheses are not balanced");
        }
    }

    @Test
    public void testQueryNotValid3() {
        try {
            Controller.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)<=Spec");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "There can only be one refinement");
        }
    }

    @Test
    public void testQueryNotValid4() {
        try {
            Controller.isQueryValid("refinement:Adm2<=(HalfAdm1(&&HalfAdm2))");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Before opening Parentheses can be either operator or second Parentheses");
        }
    }

    @Test
    public void testQueryNotValid5() {
        try {
            Controller.isQueryValid("refinement:Adm2<=(HalfAdm1||(&&HalfAdm2))");
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "After opening Parentheses can be either other Parentheses or component");
        }
    }
}
