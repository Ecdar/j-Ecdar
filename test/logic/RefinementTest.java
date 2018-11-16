package logic;

import models.Component;
import org.junit.*;
import parser.Parser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RefinementTest {

		static ArrayList<Component> machines;

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
				machines = Parser.parse();
		}

		@AfterClass
		public static void tearDownAfterClass() throws Exception {
		}

		@Test
		public void testAdmRefinesAdm() {
				Component adm = machines.get(0);
				Refinement ref = selfRefinesSelf(adm);
				assertTrue(ref.check());
		}

		@Test
		public void testMachineRefinesMachine() {
				Component machine = machines.get(1);
				Refinement ref = selfRefinesSelf(machine);
				assertTrue(ref.check());
		}

		@Test
		public void testResRefinesRes() {
				Component researcher = machines.get(2);
				Refinement ref = selfRefinesSelf(researcher);
				assertTrue(ref.check());
		}

		@Test
		public void testSpecRefinesSpec() {
				Component spec = machines.get(3);
				Refinement ref = selfRefinesSelf(spec);
				assertTrue(ref.check());
		}

		@Test
		public void testMachine3RefinesMachine3() {
				Component machine3 = machines.get(3);
				Refinement ref = selfRefinesSelf(machine3);
				assertTrue(ref.check());
		}

		@Test
		public void testCompRefinesSpec() {
				Component adm = machines.get(0);
				Component machine = machines.get(1);
				Component researcher = machines.get(2);
				Component spec = machines.get(3);

				ComposedTransitionSystem ts1 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(adm, machine, researcher)));
				SimpleTransitionSystem ts2 = new SimpleTransitionSystem(spec);

				Refinement ref = new Refinement(ts1, ts2);
				assertTrue(ref.check());
		}

		@Test
		public void testCompRefinesSelf() {
				Component adm = machines.get(0);
				Component machine = machines.get(1);
				Component researcher = machines.get(2);

				ComposedTransitionSystem ts1 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(adm, machine, researcher)));
				ComposedTransitionSystem ts2 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(machine, researcher, adm)));

				Refinement ref = new Refinement(ts1, ts2);
				assertTrue(ref.check());
		}

		private Refinement selfRefinesSelf(Component component) {
				SimpleTransitionSystem ts1 = new SimpleTransitionSystem(component);
				SimpleTransitionSystem ts2 = new SimpleTransitionSystem(component);
				return new Refinement(ts1, ts2);
		}
}