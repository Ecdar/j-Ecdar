package main;

import logic.Composition;
import models.*;
import parser.Parser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
				compositionTest();
		}

    private static void compositionTest() {
    		ArrayList<Component> machines = Parser.parse();

				Composition composition = new Composition(machines);
				State init = composition.computeInitial();
				Set<Channel> channels = new HashSet<>();
				for (Component component : machines) {
						channels.addAll(component.getActions());
				}
				for (Channel channel : channels) {
						ArrayList<State> next = composition.getNextStates(init, channel);
				}
    }
}
