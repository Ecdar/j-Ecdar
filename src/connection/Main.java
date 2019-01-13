package connection;

import logic.Controller;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String VERSION = "1.0";
    public static final String ENGINE_NAME = "JECDAR";
    private static Controller ctrl = new Controller();

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        while (console.hasNextLine()) {
            System.out.println(chooseCommand(console.nextLine()));
        }
    }

    public static String chooseCommand(String query) {
        String reader = query;
        String indicator = reader;
        if (reader.contains(" ")) {
            indicator = reader.substring(0, reader.indexOf(' '));
        }
        switch (indicator.toLowerCase()) {
            case "-version":
                return ENGINE_NAME + " Version: " + VERSION;
            case "-rq":
                try {
                    List<Boolean> temp = ctrl.handleRequest(reader.substring(reader.indexOf(' ') + 1));
                    if (temp.size() == 1) return temp.get(0).toString();
                    else {
                        String str = "";
                        for (int i = 0; i < temp.size(); i++) {
                            str = str + temp.get(i).toString();
                            if (i + 1 < temp.size()) str += " ";
                        }
                        return str;
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();//e.printStackTrace();
                }
            case "-vq":
                try {
                    boolean result = ctrl.isQueryValid(reader.substring(reader.indexOf(' ') + 1));
                    return String.valueOf(result);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();//e.printStackTrace();
                }
            case "-help":
                return    "In order to check version type:-version\n"
                        + "In order to run query type:-rq folderPath query query...\n"
                        + "In order to check the validity of a query type:-vq query";
            default:
                return "Unknown command: \"" + reader + "\"\nwrite -help to get list of commands";
        }
    }
}
//    private static void mapDBM(){
//        String fileName = "src/" + System.mapLibraryName("DBM");
//        File lib = new File(fileName);
//        System.load(lib.getAbsolutePath());

