package features;

import logic.*;
import models.Automaton;
import models.Clock;
import models.UniqueNamedContainer;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClockNamingTest {

    private static TransitionSystem t1, t2, t3, t4, machine, researcher, adm;

    private static <T> void assertAnyMatch(List<T> list, Predicate<T> predicate) {
        assertAnyMatch(list.stream(), predicate);
    }

    private static <T> void assertAnyMatch(Stream<T> stream, Predicate<T> predicate) {
        assertTrue(stream.anyMatch(predicate));
    }

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
        Conjunction con = new Conjunction(t1, t2);

        assertEquals(1, t1.getClocks().size());
        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertEquals(t1.getClocks().get(0).getUniqueName(), con.getClocks().get(0).getUniqueName());
        assertEquals("x", t1.getClocks().get(0).getUniqueName());
        assertEquals(t2.getClocks().get(0).getUniqueName(),con.getClocks().get(1).getUniqueName());
        assertEquals("y", t2.getClocks().get(0).getUniqueName());

    }

    @Test
    public void conjunctionUniqueName(){
        Conjunction con = new Conjunction(t1, t4);

        assertEquals("x",t1.getClocks().get(0).getOriginalName());
        assertEquals("x",t4.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertEquals("Test1.x", con.getClocks().get(0).getUniqueName());
        assertEquals("Test4.x", con.getClocks().get(1).getUniqueName());

    }

    @Test
    public void compositionUniqueName(){
        Composition comp = new Composition(machine, adm);

        assertEquals("y",machine.getClocks().get(0).getOriginalName());
        assertEquals("y",adm.getClocks().get(1).getOriginalName());

        assertEquals(3, comp.getClocks().size());
        assertAnyMatch(comp.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Machine.y"));
        assertAnyMatch(comp.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "x"));
        assertAnyMatch(comp.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Adm2.y"));
    }

    @Test
    public void compositionConjunctionUniqueName(){
        Composition comp1 = new Composition(machine, adm);
        Composition comp2 = new Composition(machine, researcher);
        Conjunction conj = new Conjunction(comp1, comp2);

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
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test1.x"));
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test4.x"));
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "quo_new"));
    }

    @Test
    public void quotientUniqueName2(){
        Quotient quotient = new Quotient(t1,t1);

        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(3, quotient.getClocks().size());
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test1.1.x"));
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test1.2.x"));
        assertAnyMatch(quotient.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "quo_new"));
    }

    @Test
    public void clockOwnerTest3(){
        Conjunction con = new Conjunction(t1, t1);

        assertEquals("x",t1.getClocks().get(0).getOriginalName());

        assertEquals(2, con.getClocks().size());
        assertAnyMatch(con.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test1.2.x"));
        assertAnyMatch(con.getClocks(), clock -> Objects.equals(clock.getUniqueName(), "Test1.1.x"));

    }

    @Test
    public void clockContainerTest(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));

        assertEquals(1, container.getItems().size());
        assertAnyMatch(container.getItems(), clock -> Objects.equals(clock.getUniqueName(), "x"));
    }


    @Test
    public void clockContainerTestSameNameDifferentSystem(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));
        container.add(t4.getClocks().get(0));

        assertEquals(2, container.getItems().size());
        assertAnyMatch(container.getItems(), clock -> Objects.equals(clock.getUniqueName(), "Test1.x"));
        assertAnyMatch(container.getItems(), clock -> Objects.equals(clock.getUniqueName(), "Test4.x"));
    }

    @Test
    public void clockContainerTestSameNameSameSystem(){
        UniqueNamedContainer<Clock> container = new UniqueNamedContainer<>();
        container.add(t1.getClocks().get(0));
        container.add(t1.getClocks().get(0));

        assertEquals(2, container.getItems().size());
        assertAnyMatch(container.getItems(), clock -> Objects.equals(clock.getUniqueName(), "Test1.1.x"));
        assertAnyMatch(container.getItems(), clock -> Objects.equals(clock.getUniqueName(), "Test1.2.x"));
    }
}
