package main;

import logic.ComposedTransitionSystem;
import logic.Refinement;
import logic.SimpleTransitionSystem;
import models.*;
import parser.Parser;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
				compositionTest();
		}

    private static void compositionTest() {
    		ArrayList<Component> machines = Parser.parse();
    		Component adm = machines.get(0);
				Component machine = machines.get(1);
				Component researcher = machines.get(2);
				Component spec = machines.get(3);
				Component machine3 = machines.get(4);

				ComposedTransitionSystem comp1 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(adm, machine, researcher)));
				ComposedTransitionSystem comp2 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(machine, adm, researcher)));
				ComposedTransitionSystem comp3 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(researcher, adm, machine)));

				SimpleTransitionSystem ts2 = new SimpleTransitionSystem(spec);

				Refinement ref1 = new Refinement(comp1, ts2);
				Refinement ref2 = new Refinement(comp1, comp2);
				Refinement ref3 = new Refinement(comp2, comp3);
				Refinement ref4 = new Refinement(comp3, comp1);
				Refinement ref5 = new Refinement(ts2, ts2);

				boolean c1 = ref1.check();
				boolean c2 = ref2.check();
				boolean c3 = ref3.check();
				boolean c4 = ref4.check();
				boolean c5 = ref5.check();
    }
}
