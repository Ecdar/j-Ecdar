package parser;

import logic.*;
import models.Automaton;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Before;
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
    public void testCompRefinesSpec() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=Spec", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesSpec() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Spec)<=(Spec)", false).get(0).getResult(), false);
    }

    @Test
    public void testMachNotRefinesMach() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Machine<=Machine", false).get(0).getResult(), false);
    }

    @Test
    public void testMach3NotRefinesMach3() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Machine3<=Machine3", false).get(0).getResult(), false);
    }

    @Test
    public void testMach3RefinesMach() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity","refinement:Machine3<=Machine", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesAdm() throws Exception {
        assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Spec<=Administration", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesMachine() throws Exception {
        assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity","refinement:Spec<=Machine", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesResearcher() throws Exception {
        assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Spec<=Researcher", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesMachine3() throws Exception {
        assertNotEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Spec<=Machine3", false).get(0).getResult(), true);
    }

    @Test
    public void testCompNotRefinesComp() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=(Administration||Machine||Researcher)", false).get(0).getResult(), false);
    }

    @Test
    public void testConjRefinesAdm2() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(HalfAdm1&&HalfAdm2)<=Adm2", false).get(0).getResult(), true);
    }
    @Test
    public void testDetermOne() throws Exception {
       assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "determinism:HalfAdm1", false).get(0).getResult(), true);
    }
    @Test
    public void testDetermConj() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity","determinism:HalfAdm1&&HalfAdm2", false).get(0).getResult(), true);
    }
    @Test
    public void testDetermComp() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "determinism:Administration||Machine", false).get(0).getResult(), true);
    }
    @Test
    public void testDetermCompConj() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "determinism:Administration||Machine&&Researcher", false).get(0).getResult(), true);
    }
    @Test
    public void testDetermOneFail() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "determinism:G9", false).get(0).getResult(), false);
    }
    @Test
    public void testDetermCompFail() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "determinism:G9||G10", false).get(0).getResult(), false);
    }
    @Test
    public void testDetermConjFail() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "determinism:G9&&G10", false).get(0).getResult(), false);
    }
    @Test
    public void testDetermConjCompFail() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "determinism:G9&&G10||G11", false).get(0).getResult(), false);
    }
    @Test
    public void testConsistencyOne() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "consistency:HalfAdm1", false).get(0).getResult(), true);
    }

    @Test
    public void testConsistencyConj() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "consistency:HalfAdm1&&HalfAdm2", false).get(0).getResult(), true);
    }
    @Test
    public void testConsistencyComp() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "consistency:Administration||Machine", false).get(0).getResult(), true);
    }
    @Test
    public void testConsistencyCompConj() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "consistency:Administration||Machine&&Researcher", false).get(0).getResult(), true);
    }
    @Test
    public void testImplementationComp() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "implementation:G8||G13", false).get(0).getResult(), true);
    }

//    @Test
//    public void testReturnRefinement() {
//        try {
//            assertEquals(Controller.handleRequest("-xml ./samples/xml/delayRefinement.xml refinement:T6<=T5", true).get(0).getResult(), true);
//        } catch (Exception e) {
//            fail();
//        }
//    }


    @Test
    public void testFailImplementationComp() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "implementation:G8||G15", false).get(0).getResult(), false);
    }
    @Test
    public void testImplementationOne() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "implementation:G8", false).get(0).getResult(), true);
    }
    @Test
    public void testFailImplementationOne() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/ImplTests.xml ", "implementation:G9", false).get(0).getResult(), false);
    }

    @Test
    public void testAdm2RefinesConj() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Adm2<=(HalfAdm1&&HalfAdm2)", false).get(0).getResult(), true);
    }

    @Test
    public void testSeveralQueries1() throws Exception {
        List<Query> result = Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Spec<=Spec; refinement:Machine<=Machine", false);
        assertTrue(!result.get(0).getResult() && !result.get(1).getResult());
    }

    @Test
    public void testSeveralQueries2() throws Exception {
        List<Query> result = Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=Spec; refinement:Machine3<=Machine3", false);
        assertTrue(result.get(0).getResult() && !result.get(1).getResult());
    }

    @Test
    public void testDelayRefZ3RefinesZ4() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/delayRefinement.xml", "refinement:Z3<=Z4", false).get(0).getResult(), true);
    }

    //Query validator tests

    @Test
    public void testQueryValid1() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
    }

    @Test
    public void testQueryValid2() {
        QueryParser.parse("refinement:(Administration||Researcher||Machine)<=Spec");
    }

    @Test
    public void testQueryValid3() {
        QueryParser.parse("refinement:((HalfAdm1&&HalfAdm2)||Researcher||Machine)<=Spec");
    }

    @Test
    public void testQueryValid4() {
        QueryParser.parse("refinement:((HalfAdm1&&HalfAdm2)||Researcher||(Machine1&&Machine2))<=Spec");
    }

    @Test
    public void testQueryValid5() {
        QueryParser.parse("refinement:((HalfAdm1&&(HA1||HA2))||Researcher||(Machine1&&Machine2))<=Spec");
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid1() {
        QueryParser.parse("refinsdfement:Adm2<=(HalfAdm1&&HalfAdm2)");
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid2() {
        QueryParser.parse("refinement:Adm2(<=(HalfAdm1&&HalfAdm2)");
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid3() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1&&HalfAdm2)<=Spec");
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid4() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1(&&HalfAdm2))");
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid5() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1||(&&HalfAdm2))");
    }

    @Test
    public void testQueryParsingSaveAs(){
        Query query = QueryParser.parse("get-component: Machine1 && Machine2 save-as TestMachine").get(0);

        assertEquals("TestMachine", query.getComponentName());
        assertEquals("Machine1&&Machine2", query.getSystem1());
        assertEquals(Query.QueryType.GET_COMPONENT, query.getType());
    }

    @Test
    public void testQueryParsingMultiple(){
        List<Query> queries = QueryParser.parse("get-component: Machine1 && Machine2 save-as TestMachine; refinement: Machine1 <= Machine2");

        assertEquals("TestMachine", queries.get(0).getComponentName());
        assertEquals("Machine1&&Machine2", queries.get(0).getSystem1());
        assertEquals(Query.QueryType.GET_COMPONENT, queries.get(0).getType());

        assertTrue(queries.get(1).getComponentName().startsWith("automaton"));
        assertEquals("Machine1", queries.get(1).getSystem1());
        assertEquals("Machine2", queries.get(1).getSystem2());
    }

    @Test
    public void testComponentName() throws Exception {
        Controller.parseComponents("./samples/json/EcdarUniversity", true);
        Query query = QueryParser.parse("get-component: Machine save-as TestMachine").get(0);

        assertEquals("TestMachine", query.getComponentName());
    }

    @Test
    public void testDefaultComponentName() throws Exception {
        Controller.parseComponents("./samples/json/EcdarUniversity", true);
        List<Query> queries = QueryParser.parse("get-component: Machine; get-component: Researcher");

        assertTrue(queries.get(0).getComponentName().startsWith("automaton"));
        assertTrue(queries.get(1).getComponentName().startsWith("automaton"));
        assertNotEquals(queries.get(0).getComponentName(), queries.get(1).getComponentName());
    }
}
