package features;

import logic.Refinement;
import models.Component;
import org.junit.*;
import parser.Parser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class AGTest {
		private static Component A, G, Q, Imp;

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());

				String base = "./samples/AG/";
				List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
								"Components/A.json",
								"Components/G.json",
								"Components/Q.json",
								"Components/Imp.json"));
				List<Component> machines = Parser.parse(base, components);

				A = machines.get(0);
				G = machines.get(1);
				Q = machines.get(2);
				Imp = machines.get(3);
		}

		@Test
		public void AGRefinesAImp() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(A, G)), new ArrayList<>(Arrays.asList(A, Imp)));
				assertTrue(ref.check());
		}

		@Test
		public void AImpRefinesAG() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(A, Imp)), new ArrayList<>(Arrays.asList(A, G)));
				assertTrue(ref.check());
		}

		@Test
		public void GRefinesImp() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(G)), new ArrayList<>(Arrays.asList(Imp)));
				assertTrue(ref.check());
		}

		@Test
		public void ImpNotRefinesG() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(Imp)), new ArrayList<>(Arrays.asList(G)));
				assertFalse(ref.check());
		}

		@Test
		public void GRefinesQ() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(G)), new ArrayList<>(Arrays.asList(Q)));
				assertTrue(ref.check());
		}

		@Test
		public void QRefinesG() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(Q)), new ArrayList<>(Arrays.asList(G)));
				assertTrue(ref.check());
		}

		@Test
		public void QRefinesImp() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(Q)), new ArrayList<>(Arrays.asList(Imp)));
				assertTrue(ref.check());
		}

		@Test
		public void ImpNotRefinesQ() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(Imp)), new ArrayList<>(Arrays.asList(Q)));
				assertFalse(ref.check());
		}
}
