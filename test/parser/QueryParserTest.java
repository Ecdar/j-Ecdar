package parser;

import logic.*;
import logic.query.Query;
import models.Automaton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QueryParserTest {
    private static SimpleTransitionSystem adm, machine, researcher, spec, machine3, adm2, half1, half2;

    private ArrayList<Automaton> automataList;

    @Before
    public void beforeEachTest() throws FileNotFoundException {
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

        automataList = new ArrayList<>();
        automataList.add(adm.getAutomaton());
        automataList.add(machine.getAutomaton());
        automataList.add(researcher.getAutomaton());
        automataList.add(spec.getAutomaton());
        automataList.add(machine3.getAutomaton());
        automataList.add(adm2.getAutomaton());
        automataList.add(half1.getAutomaton());
        automataList.add(half2.getAutomaton());
    }

    private TransitionSystem testVisitSystem(String systemString) throws Exception{
        CharStream charStream = CharStreams.fromString("get-component:" + systemString);
        QueryGrammar.QueryGrammarLexer lexer = new QueryGrammar.QueryGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);

        QueryGrammar.QueryGrammarParser parser = new QueryGrammar.QueryGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());
        QueryParser.SystemVisitor visitor = new QueryParser.SystemVisitor();
        Field field = QueryParser.class.getDeclaredField("automata");
        field.setAccessible(true);
        field.set(null, automataList);
        return visitor.visit(parser.queries().query(0).saveSystem().expression());
    }

    @Test
    public void testCompositionOfThree() throws Exception {
        TransitionSystem ts1 = new Composition(new TransitionSystem[]{adm, machine, researcher});
        TransitionSystem ts2 = testVisitSystem("(Administration||Machine||Researcher)");

        assertEquals(ts1, ts2);
    }

    @Test
    public void testCompositionOfOne() throws Exception {
        TransitionSystem ts = testVisitSystem("(Spec)");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfOneMultiBrackets() throws Exception {
        TransitionSystem ts = testVisitSystem("((Spec))");
        assertEquals(spec, ts);
    }

    @Test
    public void testCompositionOfThreeExtraBrackets() throws Exception {
        TransitionSystem transitionSystem1 = new Composition(new TransitionSystem[]{adm, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{transitionSystem1, researcher});
        TransitionSystem ts2 = testVisitSystem("((Administration||Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThree() throws Exception {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[]{adm, machine, researcher});
        TransitionSystem ts2 = testVisitSystem("(Administration&&Machine&&Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testConjunctionOfThreeExtraBrackets() throws Exception {
        TransitionSystem transitionSystem1 = new Conjunction(new TransitionSystem[]{adm, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{transitionSystem1, researcher});
        TransitionSystem ts2 = testVisitSystem("((Administration&&Machine)||Researcher)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery1() throws Exception {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{adm, machine, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{trs1, researcher, half1});
        TransitionSystem ts2 = testVisitSystem("((Administration&&Machine&&Machine)||Researcher||HalfAdm1)");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery2() throws Exception {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{machine, researcher});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{researcher, machine, trs1, spec});
        TransitionSystem ts2 = testVisitSystem("(Researcher||Machine||(Machine&&Researcher)||Spec)");

        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery3() throws Exception {
        TransitionSystem trs1 = new Conjunction(new TransitionSystem[]{researcher, machine});
        TransitionSystem trs2 = new Conjunction(new TransitionSystem[]{machine, researcher});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{trs1, trs2});
        TransitionSystem ts2 = testVisitSystem("((Researcher&&Machine)||(Machine&&Researcher))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void testQuery4() throws Exception {
        TransitionSystem trs1 = new Composition(new TransitionSystem[]{machine, researcher});
        TransitionSystem trs2 = new Conjunction(new TransitionSystem[]{spec, trs1, machine});
        TransitionSystem trs3 = new Conjunction(new TransitionSystem[]{machine, machine, machine});

        TransitionSystem ts1 = new Composition(new TransitionSystem[]{researcher, trs3, trs2});
        TransitionSystem ts2 = testVisitSystem("(Researcher||(Machine&&Machine&&Machine)||(Spec&&(Machine||Researcher)&&Machine))");
        assertEquals(ts1, ts2);
    }

    @Test
    public void Half1ConjHalf2() throws Exception {
        TransitionSystem ts1 = new Conjunction(new TransitionSystem[]{half1, half2});
        TransitionSystem ts2 = testVisitSystem("(HalfAdm1&&HalfAdm2)");

        assertEquals(ts1, ts2);
    }


    //Test entire Controller component

    @Test
    public void testCompRefinesSpec() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=Spec", false).get(0).getResult(), true);
    }

    @Test
    public void testSpecNotRefinesSpec() throws Exception {
        assertTrue(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Spec)<=(Spec)", false).get(0).getResult());
    }

    @Test
    public void testMachNotRefinesMach() throws Exception {
        assertEquals(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Machine<=Machine", false).get(0).getResult(), true);
    }

    @Test
    public void testMach3NotRefinesMach3() throws Exception {
        assertTrue(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:Machine3<=Machine3", false).get(0).getResult());
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
        assertTrue(Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=(Administration||Machine||Researcher)", false).get(0).getResult());
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
        assertTrue(result.get(0).getResult()); // refinement:Spec<=Spec
        assertTrue(result.get(1).getResult()); // refinement:Spec<=Spec
    }

    @Test
    public void testSeveralQueries2() throws Exception {
        List<Query> result = Controller.handleRequest("-json ./samples/json/EcdarUniversity", "refinement:(Administration||Machine||Researcher)<=Spec; refinement:Machine3<=Machine3", false);
        assertTrue(result.get(0).getResult()); // refinement:(Administration||Machine||Researcher)<=Spec
        assertTrue(result.get(1).getResult()); // refinement:Machine3<=Machine3
    }

    @Test
    public void testDelayRefZ3RefinesZ4() throws Exception {
        assertEquals(Controller.handleRequest("-xml ./samples/xml/delayRefinement.xml", "refinement:Z3<=Z4", false).get(0).getResult(), true);
    }

    //Query validator tests

    @Test
    public void testQueryValid1() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1&&HalfAdm2)", automataList);
    }

    @Test
    public void testQueryValid2() {
        QueryParser.parse("refinement:(Administration||Researcher||Machine)<=Spec", automataList);
    }

    @Test
    public void testQueryValid3() {
        QueryParser.parse("refinement:((HalfAdm1&&HalfAdm2)||Researcher||Machine)<=Spec", automataList);
    }

    @Test
    public void testQueryValid4() {
        QueryParser.parse("refinement:((HalfAdm1&&HalfAdm2)||Researcher||(Machine&&Machine3))<=Spec", automataList);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testQueryValid5() {
        QueryParser.parse("refinement:((HalfAdm1&&(HalfAdm1||HalfAdm2))||Researcher||(Machine&&Machine3))<=Spec", automataList);
    }

    @Test
    public void testQueryValidSaveAs() {
        QueryParser.parse("get-component:Adm2 save-as Adm3; refinement:Adm3<=(HalfAdm1&&HalfAdm2)", automataList);
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid1() {
        QueryParser.parse("refinsdfement:Adm2<=(HalfAdm1&&HalfAdm2)", automataList);
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid2() {
        QueryParser.parse("refinement:Adm2(<=(HalfAdm1&&HalfAdm2)", automataList);
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid3() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1&&HalfAdm2)<=Spec", automataList);
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid4() {
        QueryParser.parse("refinement:Adm2<=(HalfAdm1(&&HalfAdm2))", automataList);
    }

    @Test(expected = ParseCancellationException.class)
    public void testQueryNotValid5() {

        QueryParser.parse("refinement:Adm2<=(HalfAdm1||(&&HalfAdm2))", automataList);
    }

    @Test
    public void testQueryParsingSaveAs(){
        Query query = QueryParser.parse("get-component: HalfAdm1 && HalfAdm2 save-as TestMachine", automataList).get(0);

        assertEquals("TestMachine", query.getComponentName());
        assertEquals(Query.QueryType.GET_COMPONENT, query.getType());
    }

    @Test
    public void testQueryParsingMultiple(){
        List<Query> queries = QueryParser.parse("get-component: Machine && Machine3 save-as TestMachine; refinement: Machine <= Machine3", automataList);

        assertEquals("TestMachine", queries.get(0).getComponentName());
        assertEquals(Query.QueryType.GET_COMPONENT, queries.get(0).getType());

        assertTrue(queries.get(1).getComponentName().startsWith("automaton"));
    }

    @Test
    public void testComponentName() throws Exception {
        Query query = QueryParser.parse("get-component: Machine save-as TestMachine", automataList).get(0);

        assertEquals("TestMachine", query.getComponentName());
    }

    @Test
    public void testDefaultComponentName() throws Exception {
        List<Query> queries = QueryParser.parse("get-component: Machine; get-component: Researcher", automataList);

        assertTrue(queries.get(0).getComponentName().startsWith("automaton"));
        assertTrue(queries.get(1).getComponentName().startsWith("automaton"));
        assertNotEquals(queries.get(0).getComponentName(), queries.get(1).getComponentName());
    }
}
