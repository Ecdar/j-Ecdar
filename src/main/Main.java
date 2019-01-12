package main;

import logic.Controller;
import java.io.File;
import java.util.Scanner;

public class Main {
    public static final String VERSION = "1.0";
    public static void main(String[] args) {
        //mapDBM();

        Scanner console = new Scanner(System.in);
        System.out.println("Server has started");
        System.out.println("Waiting for input...");
        Controller ctrl = new Controller();
        while(console.hasNextLine()) {
            String reader = console.nextLine();
            String indicator = reader;
            if(reader.contains(" ")){indicator = reader.substring(0, reader.indexOf(' '));}

            switch(indicator.toLowerCase())
            {
                case "-version":
                    System.out.println("Version "+VERSION);
                    break;
                case "-rq":
                    if(reader.contains(" ")){try{System.out.println(ctrl.parseFiles(reader.substring(reader.indexOf(' ')+1,reader.length())).get(0));}
                    catch(Exception e){e.printStackTrace();}}
                    else System.out.println("Try using correct syntax -rq path query query query");
                    break;

                case "-vq":
                    try {
                        System.out.println(ctrl.isQueryValid(console.nextLine()));
                    }
                    catch(Exception e){e.printStackTrace();}
                    break;
                default:
                    System.out.println("Server confirms having received: " + reader + " Invalid command");
            }
        }
    }

    private static void mapDBM(){
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }
}
