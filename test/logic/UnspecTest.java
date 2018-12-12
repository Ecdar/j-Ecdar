package logic;

import models.Component;
import org.junit.*;
import parser.Parser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class UnspecTest {
		private static Component a, aa, b;

		@BeforeClass
		public static void setUpBeforeClass() throws Exception {
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());

				String base = "./samples/Unspec/";
				List<String> components = new ArrayList<>(Arrays.asList("GlobalDeclarations.json",
								"Components/A.json",
								"Components/AA.json",
								"Components/B.json"));
				List<Component> machines = Parser.parse(base, components);

				a = machines.get(0);
				aa = machines.get(1);
				b = machines.get(2);
		}

		@Test
		public void compRefinesB() {
				Refinement ref = new Refinement(new ArrayList<>(Arrays.asList(a, aa)), new ArrayList<>(Arrays.asList(b)));
				assertTrue(ref.check());
		}
}
