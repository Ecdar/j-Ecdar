package features;

import logic.*;
import models.Automaton;
import models.Clock;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClockNamingTest {

    private static TransitionSystem t1, t2, t3, t4, machine, researcher, adm;

    @BeforeClass
    public static void setUpBeforeClass() {
        String conjunctionBase = "./samples/json/Conjunction/";
        String[] conjunctionComponents = new String[]{"GlobalDeclarations.json",
                "Components/Test1.json",
                "Components/Test2.json",
                "Components/Test3.json",
                "Components/Test4.json"};
        Automaton[] conjunctionMachines = JSONParser.parse(conjunctionBase, conjunctionComponents, true);

        t1 = new SimpleTransitionSystem(conjunctionMachines[0]);
        t2 = new SimpleTransitionSystem(conjunctionMachines[1]);
        t3 = new SimpleTransitionSystem(conjunctionMachines[2]);
        t4 = new SimpleTransitionSystem(conjunctionMachines[3]);

        String univsersityBase = "./samples/json/EcdarUniversity/";
        String[] univsersityComponents = new String[]{"GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"};
        Automaton[] universityMachines = JSONParser.parse(univsersityBase, univsersityComponents, true);

        machine = new SimpleTransitionSystem(universityMachines[1]);
        researcher = new SimpleTransitionSystem(universityMachines[2]);
        adm = new SimpleTransitionSystem(universityMachines[5]);
    }

    @Test
    public void conjunctionUniqueNameControl(){
        Conjunction con = new Conjunction(new TransitionSystem[]{t1, t2});

        assertEquals(1, t1.getClocks().size());
        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertEquals("x",con.getClocks().get(0).getUniqueName());
        assertEquals("x", t1.getClocks().get(0).getUniqueName());
        assertEquals("y",con.getClocks().get(1).getUniqueName());
        assertEquals("y",t2.getClocks().get(0).getUniqueName());

    }

    @Test
    public void conjunctionUniqueName(){
        Conjunction con = new Conjunction(new TransitionSystem[]{t1, t4});

        assertEquals("x",t1.getClocks().get(0).getOriginalName());
        assertEquals("x",t4.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertEquals("Test1.x", con.getClocks().get(0).getUniqueName());
        assertEquals("Test4.x", con.getClocks().get(1).getUniqueName());

    }

    @Test
    public void compositionUniqueName(){
        Composition comp = new Composition(new TransitionSystem[]{machine, adm});

        assertEquals("y",machine.getClocks().get(0).getOriginalName());
        assertEquals("y",adm.getClocks().get(1).getOriginalName());

        assertEquals(3, comp.getClocks().size());
        assertEquals("Machine.y", comp.getClocks().get(0).getUniqueName());
        assertEquals("Adm2.y", comp.getClocks().get(2).getUniqueName());

    }

    @Test
    public void compositionConjunctionUniqueName(){
        Composition comp1 = new Composition(new TransitionSystem[]{machine, adm});
        Composition comp2 = new Composition(new TransitionSystem[]{machine, researcher});
        Conjunction conj = new Conjunction(new TransitionSystem[]{comp1, comp2});

        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        List<String> names = conj.getClocks().stream().map(Clock::getUniqueName).collect(Collectors.toList());
        assertEquals(5, names.size());
        assertTrue("Machine.1.y", names.contains("Machine.1.y"));
        assertTrue("Machine.2.y", names.contains("Machine.2.y"));
        assertTrue("Adm2.y", names.contains("Adm2.y"));
        assertTrue("Adm2.x", names.contains("Adm2.x"));
        assertTrue("Researcher.x", names.contains("Researcher.x"));

    }

    @Test
    public void quotientUniqueName(){
        Quotient quotient = new Quotient(t1,t4);

        assertEquals("x",t1.getClocks().get(0).getOriginalName());
        assertEquals("x",t4.getClocks().get(0).getOriginalName());

        assertEquals(3, quotient.getClocks().size());
        assertEquals("Test1.x", quotient.getClocks().get(1).getUniqueName());
        assertEquals("Test4.x", quotient.getClocks().get(2).getUniqueName());

    }

    @Test
    public void quotientUniqueName2(){
        Quotient quotient = new Quotient(t1,t1);

        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(3, quotient.getClocks().size());
        assertEquals("Test1.1.x", quotient.getClocks().get(1).getUniqueName());
        assertEquals("Test1.2.x", quotient.getClocks().get(2).getUniqueName());

    }

    @Test
    public void clockOwnerTest3(){
        Conjunction con = new Conjunction(new TransitionSystem[]{t1, t1});

        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertEquals("Test1.1.x", con.getClocks().get(0).getUniqueName());
        assertEquals("Test1.2.x", con.getClocks().get(1).getUniqueName());

    }

    @Test
    public void clockContainerTest(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));

        assertEquals(1, container.getItems().size());
        assertEquals("x", container.getItems().get(0).getUniqueName());
    }


    @Test
    public void clockContainerTestSameNameDifferentSystem(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));
        container.add(t4.getClocks().get(0));

        assertEquals(2, container.getItems().size());
        assertEquals("Test1.x", container.getItems().get(0).getUniqueName());
        assertEquals("Test4.x", container.getItems().get(1).getUniqueName());
    }

    @Test
    public void clockContainerTestSameNameSameSystem(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));
        container.add(t1.getClocks().get(0));

        assertEquals(2, container.getItems().size());
        assertEquals("Test1.1.x", container.getItems().get(0).getUniqueName());
        assertEquals("Test1.2.x", container.getItems().get(1).getUniqueName());
    }
}
