package connection;

import logic.*;

import java.util.List;
import java.util.Scanner;

import models.Automaton;
import org.apache.commons.cli.*;
import parser.JSONParser;
import parser.XMLParser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class Main {
    static final String VERSION = "1.0";
    static final String ENGINE_NAME = "JECDAR";


    static Options options = new Options();

    static Option inputFolder = Option.builder("i")
            .longOpt("input-folder")
            .argName("file")
            .hasArg()
            .desc("Provided input folder")
            .required(true)
            .build();

    static Option comps = Option.builder()
            .longOpt("components")
            .argName("comps")
            .hasArgs()
            .desc("Components to load")
            .required(false)
            .build();

    static Option refinement = Option.builder()
            .longOpt("refinement")
            .argName("ref")
            .hasArg()
            .desc("Query for refinement check")
            .required(false)
            .build();

    static Option get = Option.builder()
            .longOpt("get-new-component")
            .argName("get")
            .hasArg()
            .desc("Produce an automaton for the query")
            .required(false)
            .build();

    static Option quo = Option.builder()
            .longOpt("quotient")
            .argName("quo")
            .hasArg()
            .desc("produce the quotient")
            .required(false)
            .build();

    static Option bsim = Option.builder()
            .longOpt("bisim-min")
            .argName("bsim")
            .hasArg(false)
            .desc("minimize the automaton via bisimilarity check")
            .required(false)
            .build();

    static Option prn = Option.builder()
            .longOpt("prune")
            .argName("prn")
            .hasArg(false)
            .desc("prune inconsistent states (name needs to be inc) from the automata")
            .required(false)
            .build();


    Option spec = new Option("specification", "check if this is a specification");
    Option imp = new Option("implementation", "check if this is an implementation");
    Option lcon = new Option("local-consistency", "check for local consistency");
    Option gcon = new Option("global-consistency", "check for global consistency");

    public static void main(String[] args) {

        options.addOption(inputFolder);
        options.addOption(refinement);
        options.addOption(comps);
        options.addOption(quo);
        options.addOption(get);
        options.addOption(prn);
        options.addOption(bsim);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);


            String refQuery = cmd.getOptionValue("refinement");
            String quotientQuery = cmd.getOptionValue("quotient");
            String inputFolderPath = cmd.getOptionValue("inputFolder");
            String getNewComp = cmd.getOptionValue("get-new-component");
            String[] components = cmd.getOptionValues("comps");
            Automaton[] machines = JSONParser.parse(inputFolderPath, false);

            boolean prune = cmd.hasOption("prune");
            boolean bisim = cmd.hasOption("bsim-min");



            if (getNewComp != null )
            {
                try {
                TransitionSystem tr = Controller.handleRequestGetComp("-json " + inputFolderPath + " " + getNewComp,false);
                Automaton aut = tr.getAutomaton();
                if (prune)
                {
                    SimpleTransitionSystem simp = Pruning.pruneIncTimed(new SimpleTransitionSystem(aut));
                    aut = simp.pruneReachTimed().getAutomaton();
                }

                if (bisim)
                {
                    aut = Bisimilarity.checkBisimilarity(aut);
                }

                JsonFileWriter.writeToJson(aut, "./teeeeeest/");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    fail();
                }
            }

            if (refQuery !=null) {
                try {
                    System.out.println("-json " + inputFolderPath + " " + refQuery);
                    System.out.println(Controller.handleRequest("-json " + inputFolderPath + " " + refQuery, false));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    fail();
                }
            }


        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

    }


    static String chooseCommand(String query) {
        String indicator = query;
        if (query.contains(" ")) {
            indicator = query.substring(0, query.indexOf(' '));
        }

        switch (indicator.toLowerCase()) {
            case "-version":
                return ENGINE_NAME + " Version: " + VERSION;
            case "-rq":
                try {
                    List<String> temp = Controller.handleRequest(query.substring(query.indexOf(' ') + 1), false);
                    if (temp.size() == 1) return temp.get(0);
                    else {
                        StringBuilder str = new StringBuilder();
                        for (int i = 0; i < temp.size(); i++)
                            str.append(temp.get(i));
                        return str.toString();
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();//e.printStackTrace();
                }
            case "-rqrrr":
                try {
                    List<String> temp = Controller.handleRequest(query.substring(query.indexOf(' ') + 1), true);
                    if (temp.size() == 1) return temp.get(0);
                    else {
                        StringBuilder str = new StringBuilder();
                        for (int i = 0; i < temp.size(); i++)
                            str.append(temp.get(i));
                        return str.toString();
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();//e.printStackTrace();
                }
            case "-vq":
                try {
                    Controller.isQueryValid(query.substring(query.indexOf(' ') + 1));
                    return String.valueOf(true);
                } catch (Exception e) {
                    return "Error: " + e.getMessage();//e.printStackTrace();
                }
            case "-help":
                return "In order to check version type:-version\n"
                        + "In order to run query type:-rq -json/-xml folderPath query query...\n"
                        + "In order to check the validity of a query type:-vq query";
            default:
                return "Unknown command: \"" + query + "\"\nwrite -help to get list of commands";
        }
    }
}
