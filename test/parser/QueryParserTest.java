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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        TransitionSystem transitionSystem1 = new Composition(ts);
        assertTrue(transitionSystem1.equals(ctrl.runQuery("(Administration||Machine||Researcher)")));
    }
    @Test
    public void testCompositionOfOne() {
        SimpleTransitionSystem ts = new SimpleTransitionSystem(spec);
        assertTrue(ts.equals(ctrl.runQuery("(Spec)")));
    }
    @Test
    public void testCompositionOfOneMultiBrackets() {
        SimpleTransitionSystem ts = new SimpleTransitionSystem(spec);
        assertTrue(ts.equals(ctrl.runQuery("Spec")));
    }
    @Test
    public void testCompositionOfThreeExtraBrackets() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem transitionSystem1 = new Composition(ts);
        ArrayList<TransitionSystem> ts2 = new ArrayList<>();
        ts2.add(transitionSystem1);
        ts2.add(new SimpleTransitionSystem(researcher));

        TransitionSystem transitionSystem = new Composition(ts2);

        assertTrue(transitionSystem.equals(ctrl.runQuery("((Administration||Machine)||Researcher)")));
    }
    @Test
    public void testConjunctionOfThree() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(researcher));
        TransitionSystem transitionSystem1 = new Conjunction(ts);
        assertTrue(transitionSystem1.equals(ctrl.runQuery("(Administration&&Machine&&Researcher)")));
    }
    @Test
    public void testConjunctionOfThreeExtraBrackets() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem transitionSystem1 = new Conjunction(ts);
        ArrayList<TransitionSystem> ts2 = new ArrayList<>();
        ts2.add(transitionSystem1);
        ts2.add(new SimpleTransitionSystem(researcher));

        TransitionSystem transitionSystem = new Composition(ts2);

        assertTrue(transitionSystem.equals(ctrl.runQuery("((Administration&&Machine)||Researcher)")));
    }

    @Test
    public void testQuery() {
        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(adm));
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(machine));
        TransitionSystem ts1 = new Conjunction(ts);
        ArrayList<TransitionSystem> ts2 = new ArrayList<>();
        ts2.add(ts1);
        ts2.add(new SimpleTransitionSystem(researcher));
        ts2.add(new SimpleTransitionSystem(half1));
        TransitionSystem transitionSystem = new Composition(ts2);

        assertTrue(transitionSystem.equals(ctrl.runQuery("((Administration&&Machine&&Machine)||Researcher||HalfAdm1)")));
    }

    @Test
    public void testQuery3() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(researcher));
        ts0.add(new SimpleTransitionSystem(machine));


        ArrayList<TransitionSystem> ts = new ArrayList<>();
        ts.add(new SimpleTransitionSystem(machine));
        ts.add(new SimpleTransitionSystem(researcher));
        TransitionSystem ts1 = new Conjunction(ts);
        ts0.add(ts1);
        ts0.add(new SimpleTransitionSystem(spec));
        TransitionSystem tsc = new Composition(ts0);

        assertTrue(tsc.equals(ctrl.runQuery("(Researcher||Machine||(Machine&&Researcher)||Spec)")));
    }
    @Test
    public void testQuery4() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(researcher));
        ts0.add(new SimpleTransitionSystem(machine));
        TransitionSystem ts1 = new Conjunction(ts0);

        ArrayList<TransitionSystem> ts2 = new ArrayList<>();
        ts2.add(new SimpleTransitionSystem(machine));
        ts2.add(new SimpleTransitionSystem(researcher));
        TransitionSystem ts3 = new Conjunction(ts2);
        ArrayList<TransitionSystem> tss = new ArrayList<>();
        tss.add(ts1);
        tss.add(ts3);
        TransitionSystem tsc = new Composition(tss);
        TransitionSystem tscs = ctrl.runQuery("((Researcher&&Machine)||(Machine&&Researcher))");
        assertTrue(tsc.equals(tscs));
    }
    @Test
    public void testQuery5() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(researcher));



        ArrayList<TransitionSystem> ts2 = new ArrayList<>();
        ts2.add(new SimpleTransitionSystem(machine));
        ts2.add(new SimpleTransitionSystem(machine));
        ts2.add(new SimpleTransitionSystem(machine));
        TransitionSystem ts3 = new Conjunction(ts2);
        ts0.add(ts3);

        ArrayList<TransitionSystem> tss = new ArrayList<>();
        tss.add(new SimpleTransitionSystem(spec));


        ArrayList<TransitionSystem> ts5 = new ArrayList<>();
        ts5.add(new SimpleTransitionSystem(machine));
        ts5.add(new SimpleTransitionSystem(researcher));
        TransitionSystem ts6 = new Composition(ts5);

        tss.add(ts6);
        tss.add(new SimpleTransitionSystem(machine));
        TransitionSystem ts7 = new Conjunction(tss);
        ts0.add(ts7);
        TransitionSystem last = new Composition(ts0);
        TransitionSystem tscs = ctrl.runQuery("(Researcher||(Machine&&Machine&&Machine)||(Spec&&(Machine||Researcher)&&Machine))");
        assertTrue(last.equals(tscs));
    }
    @Test
    public void Half1ConjHalf2() {
        ArrayList<TransitionSystem> ts0 = new ArrayList<>();
        ts0.add(new SimpleTransitionSystem(half1));
        ts0.add(new SimpleTransitionSystem(half2));
        TransitionSystem tsConj = new Conjunction(ts0);
        TransitionSystem tscs = ctrl.runQuery("(HalfAdm1&&HalfAdm2)");
        assertTrue(tsConj.equals(tscs));
    }
    @Test
    public void CompRefinesSpec() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:(Administration||Machine||Researcher)<=Spec");
        assertTrue(result.get(0));
    }
    @Test
    public void SpecRefinesSpec() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:(Spec)<=(Spec)");
        assertTrue(result.get(0));
    }
    @Test
    public void MachRefinesMach() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Machine<=Machine");
        assertTrue(result.get(0));
    }
    @Test
    public void Mach3RefinesMach3() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Machine3<=Machine3");
        assertTrue(result.get(0));
    }
    @Test
    public void Mach3RefinesMach() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Machine3<=Machine");
        assertTrue(result.get(0));
    }
    @Test
    public void testSpecNotRefinesAdm() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Spec<=Administration");
        assertFalse(result.get(0));
    }

    @Test
    public void testSpecNotRefinesMachine() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Spec<=Machine");
        assertFalse(result.get(0));
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Spec<=Researcher");
        assertFalse(result.get(0));
    }

    @Test
    public void testSpecNotRefinesMachine3() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Spec<=Machine3");
        assertFalse(result.get(0));
    }
    @Test
    public void CompRefinesComp() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:(Administration||Machine||Researcher)<=(Administration||Machine||Researcher)");
        assertTrue(result.get(0));
    }
    @Test
    public void ConjRefinesAdm2() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:(HalfAdm1&&HalfAdm2)<=Adm2");
        assertTrue(result.get(0));
    }
    @Test
    public void Adm2RefinesConj() {
        List<Boolean> result = ctrl.parseFiles("./samples/EcdarUniversity refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
        assertTrue(result.get(0));
    }
    @Test
    public void QueryValidity() {
        try {
            boolean result = ctrl.isQueryValid("refinement:Adm2<=(HalfAdm1&&HalfAdm2)");
            assertTrue(result);
        }
       catch (Exception e){

       }
    }
//    @Test
//    public void QueryValidity2() {
//        try {
//            boolean result = ctrl.isQueryValid("refinsdfement:Adm2<=(HalfAdm1&&HalfAdm2)");
//        }
//        catch (Exception e){
//
//            assertTrue(e);
//        }
//    }
}
