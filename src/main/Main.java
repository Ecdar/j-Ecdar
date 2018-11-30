package main;

import models.*;
import parser.Parser;
import java.util.*;

public class Main {

    public static void main(String[] args) {
				compositionTest();
		}

    private static void compositionTest() {
				List<Component> machines = Parser.parse();
				Component adm = machines.get(0);
				Component machine = machines.get(1);
				Component researcher = machines.get(2);
				Component spec = machines.get(3);
				Component machine3 = machines.get(4);
		}
}