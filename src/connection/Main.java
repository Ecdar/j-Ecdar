package connection;

import logic.*;

import java.util.List;
import java.util.Scanner;

import models.Automaton;
import org.apache.commons.cli.*;
import parser.JSONParser;
import parser.XMLParser;


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

    static Option outputFolder = Option.builder("o")
            .longOpt("output-folder")
            .hasArg()
            .build();

    static Option help = Option.builder("h")
            .longOpt("help")
            .build();


    public static void main(String[] args) {

        options.addOption(inputFolder);
        options.addOption(outputFolder);
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
            String outputFolderPath = cmd.getOptionValue("output-folder");
            String[] components = cmd.getOptionValues("comps");
            Automaton[] machines = JSONParser.parse(inputFolderPath, false);

            List<String> argsList = cmd.getArgList();
            StringBuilder argStrBuilder = new StringBuilder();
            argsList.forEach(argStrBuilder::append);
            String queryString = argStrBuilder.toString();

            try {
                System.out.println("-json " + inputFolderPath + " " + queryString);
                System.out.println(Controller.handleRequest("-json " + inputFolderPath, outputFolderPath, queryString, false));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
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
