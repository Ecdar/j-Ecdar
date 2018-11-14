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

    		ComposedTransitionSystem ts1 = new ComposedTransitionSystem(new ArrayList<>(Arrays.asList(machines.get(0), machines.get(1), machines.get(2))));
				SimpleTransitionSystem ts2 = new SimpleTransitionSystem(machines.get(3));

				Refinement ref = new Refinement(ts1, ts2);
				ref.check();
    }
}
