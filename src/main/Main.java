package main;

import logic.Controller;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        //mapDBM();
        Scanner console = new Scanner(System.in);
        System.out.println("Server has started");
        System.out.println("Waiting for input...");
        Controller ctrl = new Controller();
        while(console.hasNextLine()) {
            String reader = console.nextLine();
            switch(reader)
            {
                case "version":
                    System.out.println("Version 1.0");
                    break;
                case "path":
                    //System.out.println();

                    String base = "D:\\\\Ecdar2-2\\\\samples\\\\EcdarUniversity,refinement: (Administration || Machine || Researcher) <= Spec";
                    System.out.println(ctrl.parseFiles(console.nextLine()).get(0));
                    //System.out.println(console.nextLine());


                    break;
                default:
                    System.out.println("Server confirms having received: " + reader + " Invalid command");
            }
        }


        String base = "D:\\Ecdar2-2\\samples\\EcdarUniversity," + "refinement: (Administration || Machine || Researcher) <= Spec";
        System.out.println(ctrl.parseFiles(base).get(0));
    }
    private static void mapDBM(){
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }
}
