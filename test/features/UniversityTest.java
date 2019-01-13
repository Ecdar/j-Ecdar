package features;

import logic.Composition;
import logic.Conjunction;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.Component;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static features.Helpers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversityTest {

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
    }

    @Test
    public void testAdm2RefinesAdm2() {
        Refinement ref = selfRefinesSelf(adm2);
        assertTrue(ref.check());
    }

    @Test
    public void testHal1RefinesHalf1() {
        Refinement ref = selfRefinesSelf(half1);
        assertTrue(ref.check());
    }

    @Test
    public void testHalf2RefinesHalf2() {
        Refinement ref = selfRefinesSelf(half2);
        assertTrue(ref.check());
    }

    @Test
    public void testAdmRefinesAdm() {
        Refinement ref = selfRefinesSelf(adm);
        assertTrue(ref.check());
    }

    @Test
    public void testAdmNotRefinesMachine() {
        Refinement ref = simpleRefinesSimple(adm, machine);
        assertFalse(ref.check());
    }

    @Test
    public void testAdmNotRefinesResearcher() {
        Refinement ref = simpleRefinesSimple(adm, researcher);
        assertFalse(ref.check());
    }

    @Test
    public void testAdmNotRefinesSpec() {
        Refinement ref = simpleRefinesSimple(adm, spec);
        assertFalse(ref.check());
    }

    @Test
    public void testAdmNotRefinesMachine3() {
        Refinement ref = simpleRefinesSimple(adm, machine3);
        assertFalse(ref.check());
    }

    @Test
    public void testMachineRefinesMachine() {
        Refinement ref = selfRefinesSelf(machine);
        assertTrue(ref.check());
    }

    @Test
    public void testMachineNotRefinesAdm() {
        Refinement ref = simpleRefinesSimple(machine, adm);
        assertFalse(ref.check());
    }

    @Test
    public void testMachineNotRefinesResearcher() {
        Refinement ref = simpleRefinesSimple(machine, researcher);
        assertFalse(ref.check());
    }

    @Test
    public void testMachineNotRefinesSpec() {
        Refinement ref = simpleRefinesSimple(machine, spec);
        assertFalse(ref.check());
    }

    @Test
    public void testMachineNotRefinesMachine3() {
        Refinement ref = simpleRefinesSimple(machine, machine3);
        assertFalse(ref.check());
    }

    @Test
    public void testResRefinesRes() {
        Refinement ref = selfRefinesSelf(researcher);
        assertTrue(ref.check());
    }

    @Test
    public void testResNotRefinesAdm() {
        Refinement ref = simpleRefinesSimple(researcher, adm);
        assertFalse(ref.check());
    }

    @Test
    public void testResNotRefinesMachine() {
        Refinement ref = simpleRefinesSimple(researcher, machine);
        assertFalse(ref.check());
    }

    @Test
    public void testResNotRefinesSpec() {
        Refinement ref = simpleRefinesSimple(researcher, spec);
        assertFalse(ref.check());
    }

    @Test
    public void testResNotRefinesMachine3() {
        Refinement ref = simpleRefinesSimple(researcher, machine3);
        assertFalse(ref.check());
    }

    @Test
    public void testSpecRefinesSpec() {
        Refinement ref = selfRefinesSelf(spec);
        assertTrue(ref.check());
    }

    @Test
    public void testSpecNotRefinesAdm() {
        Refinement ref = simpleRefinesSimple(spec, adm);
        assertFalse(ref.check());
    }

    @Test
    public void testSpecNotRefinesMachine() {
        Refinement ref = simpleRefinesSimple(spec, machine);
        assertFalse(ref.check());
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        Refinement ref = simpleRefinesSimple(spec, researcher);
        assertFalse(ref.check());
    }

    @Test
    public void testSpecNotRefinesMachine3() {
        Refinement ref = simpleRefinesSimple(spec, machine3);
        assertFalse(ref.check());
    }

    @Test
    public void testMachine3RefinesMachine3() {
        Refinement ref = selfRefinesSelf(machine3);
        assertTrue(ref.check());
    }

    @Test
    public void testMachine3RefinesMachine() {
        Refinement ref = simpleRefinesSimple(machine3, machine);
        assertTrue(ref.check());
    }

    @Test
    public void testMachine3NotRefinesAdm() {
        Refinement ref = simpleRefinesSimple(machine3, adm);
        assertFalse(ref.check());
    }

    @Test
    public void testMachine3NotRefinesResearcher() {
        Refinement ref = simpleRefinesSimple(machine3, researcher);
        assertFalse(ref.check());
    }

    @Test
    public void testMachine3NotRefinesSpec() {
        Refinement ref = simpleRefinesSimple(machine3, spec);
        assertFalse(ref.check());
    }

    @Test
    public void testCompRefinesSpec() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(
                        new SimpleTransitionSystem(adm), new SimpleTransitionSystem(machine), new SimpleTransitionSystem(researcher)))),
                new SimpleTransitionSystem(spec));
        assertTrue(ref.check());
    }

    @Test
    public void testCompRefinesSelf() {
        Refinement ref = new Refinement(
                new Composition(new ArrayList<>(Arrays.asList(
                        new SimpleTransitionSystem(adm), new SimpleTransitionSystem(machine), new SimpleTransitionSystem(researcher)))),
                new Composition(new ArrayList<>(Arrays.asList(
                        new SimpleTransitionSystem(machine), new SimpleTransitionSystem(researcher), new SimpleTransitionSystem(adm)))));
        assertTrue(ref.check());
    }

    @Test
    public void testUncomposable() {
        boolean fail = false;

        try {
            Refinement ref = new Refinement(
                    new Composition(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(machine), new SimpleTransitionSystem(machine3)))),
                    new SimpleTransitionSystem(machine));
        } catch (IllegalArgumentException ex) {
            fail = true;
        }

        assertTrue(fail);
    }

    @Test
    public void testHalf1AndHalf2RefinesAdm2() {
        Refinement ref = new Refinement(
                new Conjunction(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(half1), new SimpleTransitionSystem(half2)))),
                new SimpleTransitionSystem(adm2));

        assertTrue(ref.check());
    }

    @Test
    public void testAdm2RefinesHalf1AndHalf2() {
        Refinement ref = new Refinement(
                new SimpleTransitionSystem(adm2),
                new Conjunction(new ArrayList<>(Arrays.asList(new SimpleTransitionSystem(half1), new SimpleTransitionSystem(half2)))));

        assertTrue(ref.check());
    }
}