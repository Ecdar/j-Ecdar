package connection;

import logic.*;

import java.util.List;
import org.apache.commons.cli.*;


class Main {
    static final String VERSION = "1.0";
    static final String ENGINE_NAME = "JECDAR";

    static Options options = new Options();

    static Option proto = Option.builder("p")
            .longOpt("proto")
            .argName("port")
            .hasArg()
            .type(Number.class)
            .build();

    static Option inputFolder = Option.builder("i")
            .longOpt("input-folder")
            .argName("file")
            .hasArg()
            .desc("Provided input folder")
            .build();

    static Option outputFolder = Option.builder("o")
            .longOpt("output-folder")
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
                int port = ((Number)cmd.getParsedOptionValue("proto")).intValue();
                GrpcServer server = new GrpcServer(port);
                try {
                    server.start();
                    server.blockUntilShutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String inputFolderPath = cmd.getOptionValue("input-folder");
            String outputFolderPath = cmd.getOptionValue("output-folder");

            if(inputFolderPath == null){
                printHelp(formatter,options);
                return;
            }

            List<String> argsList = cmd.getArgList();
            StringBuilder argStrBuilder = new StringBuilder();
            argsList.forEach(argStrBuilder::append);
            String queryString = argStrBuilder.toString();

            try {
                System.out.println(inputFolderPath + " " + queryString);
                if(inputFolderPath.endsWith(".xml")){
                    System.out.println(Controller.handleRequest("-xml " + inputFolderPath, outputFolderPath, queryString, false));
                }else{
                    System.out.println(Controller.handleRequest("-json " + inputFolderPath, outputFolderPath, queryString, false));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
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
