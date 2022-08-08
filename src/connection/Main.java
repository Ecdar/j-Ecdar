package connection;

import logic.*;
import logic.query.Query;
import models.Automaton;
import models.CDD;
import models.Clock;
import org.apache.commons.cli.*;
import parser.JSONParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class Main {
    static final String VERSION = "1.0";
    static final String ENGINE_NAME = "JECDAR";

    static Options options = new Options();

    static Option proto = Option.builder("p")
            .longOpt("proto")
            .argName("address")
            .hasArg()
            .type(Number.class)
            .build();

    static Option inputFolder = Option.builder("i")
            .longOpt("input-folder")
            .argName("file")
            .hasArg()
            .desc("Provided input folder")
            .build();

    static Option outputFolder = Option.builder("s")
            .longOpt("save-to-disk")
            .hasArg()
            .build();

    static Option help = Option.builder("h")
            .longOpt("help")
            .build();


    public static void main(String[] args) {
        options.addOption(proto);
        options.addOption(outputFolder);
        options.addOption(inputFolder);
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

            if(cmd.hasOption("proto")){
                String address = cmd.getOptionValue("proto");
                GrpcServer server = new GrpcServer(address);
                try {
                    server.start();
                    server.blockUntilShutdown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            String inputFolderPath = cmd.getOptionValue("input-folder");

            if(inputFolderPath == null){
                printHelp(formatter,options);
                return;
            }

            List<String> argsList = cmd.getArgList();
            StringBuilder argStrBuilder = new StringBuilder();
            argsList.forEach(argStrBuilder::append);
            String queryString = argStrBuilder.toString();

            try {
                List<Query> queries = new ArrayList<>();
                if(inputFolderPath.endsWith(".xml")){
                    queries = Controller.handleRequest("-xml " + inputFolderPath, queryString, false);
                }else{
                    queries = Controller.handleRequest("-json " + inputFolderPath, queryString, false);
                }
                for (Query query: queries) {
                    System.out.println(query.getResult());
                    System.out.println(query.getResultStrings());
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

            if(cmd.hasOption("save-to-disk")){
                String outputFolderPath = cmd.getOptionValue("save-to-disk");
                Controller.saveToDisk(outputFolderPath);
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(formatter,options);
        }

    }

    private static void printHelp(HelpFormatter formatter, Options options){
        formatter.printHelp("-i path/to/folder [OPTIONS] [\"QUERIES\"]", options);
    }

}
