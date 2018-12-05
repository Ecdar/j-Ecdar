package logic;

import models.Component;
import org.junit.*;
import parser.Parser;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class BigRefinementTest {

		private static Component comp1, ref1;

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());
				List<Component> machines = Parser.parse();
				comp1 = machines.get(0);
				ref1 = machines.get(1);
		}

		@Test
		public void testRef1RefinesComp() {
				Refinement ref = simpleRefinesSimple(ref1, comp1);
				assertTrue(ref.check());
		}

		// helper functions
		private Refinement selfRefinesSelf(Component component) {
				return simpleRefinesSimple(component, component);
		}

		private Refinement simpleRefinesSimple(Component component1, Component component2) {
				TransitionSystem ts1 = new SimpleTransitionSystem(component1);
				TransitionSystem ts2 = new SimpleTransitionSystem(component2);
				return new Refinement(ts1, ts2);
		}
		//
}