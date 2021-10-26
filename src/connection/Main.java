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

    static Option help = Option.builder("h")
            .longOpt("help")
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
            .desc("Query for refinement check")
            .required(false)
            .build();

    static Option get = Option.builder()
            .longOpt("get-new-component")
            .desc("Produce an automaton for the query")
            .required(false)
            .build();

    static Option quo = Option.builder()
            .longOpt("quotient")
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
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if(cmd.hasOption("help")){
                printHelp(formatter,options);
                return;
            }

            String inputFolderPath = cmd.getOptionValue("input-folder");
            String[] components = cmd.getOptionValues("comps");
            Automaton[] machines = JSONParser.parse(inputFolderPath, false);

            List<String> argsList = cmd.getArgList();
            StringBuilder argStrBuilder = new StringBuilder();
            argsList.forEach(argStrBuilder::append);
            String queryString = argStrBuilder.toString();

            boolean prune = cmd.hasOption("prune");
            boolean bisim = cmd.hasOption("bsim-min");
            boolean refinment = cmd.hasOption("refinement");
            boolean getNewComponent = cmd.hasOption("get-new-component");

            try {
                System.out.println("-json " + inputFolderPath + " " + queryString);
                System.out.println(Controller.handleRequest("-json " + inputFolderPath, queryString, false));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                fail();
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(formatter,options);

            System.exit(1);
        }

    }

    private static void printHelp(HelpFormatter formatter, Options options){
        formatter.printHelp("-i path/to/folder [OPTIONS] [\"QUERIES\"]", options);
    }
}
