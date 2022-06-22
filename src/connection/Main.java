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

        TransitionSystem adm, admCopy, machine, machineCopy, researcher, researcherCopy, spec, specCopy,
                machine3, machine3Copy, adm2, adm2Copy, half1, half1Copy, half2, half2Copy;

        String base = "./samples/json/EcdarUniversity/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);
        CDD.init(100,100,100);
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(machines[0].getClocks());
        clocks.addAll(machines[1].getClocks());
        clocks.addAll(machines[2].getClocks());
        clocks.addAll(machines[3].getClocks());
        clocks.addAll(machines[4].getClocks());
        clocks.addAll(machines[5].getClocks());
        clocks.addAll(machines[6].getClocks());
        clocks.addAll(machines[7].getClocks());
        CDD.addClocks(clocks);

        adm = new SimpleTransitionSystem((machines[0]));
        admCopy = new SimpleTransitionSystem(new Automaton((machines[0])));
        machine = new SimpleTransitionSystem((machines[1]));
        machineCopy = new SimpleTransitionSystem(new Automaton((machines[1])));
        researcher = new SimpleTransitionSystem((machines[2]));
        researcherCopy = new SimpleTransitionSystem(new Automaton((machines[2])));
        spec = new SimpleTransitionSystem((machines[3]));
        specCopy = new SimpleTransitionSystem(new Automaton((machines[3])));
        machine3 = new SimpleTransitionSystem((machines[4]));
        machine3Copy = new SimpleTransitionSystem(new Automaton((machines[4])));
        adm2 = new SimpleTransitionSystem((machines[5]));
        adm2Copy = new SimpleTransitionSystem(new Automaton((machines[5])));
        half1 = new SimpleTransitionSystem((machines[6]));
        half1Copy = new SimpleTransitionSystem(new Automaton((machines[6])));
        half2 = new SimpleTransitionSystem((machines[7]));
        half2Copy = new SimpleTransitionSystem(new Automaton((machines[7])));
        CDD.done();

        Quotient q = new Quotient(spec,machine);
        Refinement ref = new Refinement(new Composition(new TransitionSystem[]{researcher,adm}), new SimpleTransitionSystem(q.getAutomaton()) );
        boolean res = ref.check();
        System.out.println(ref.getErrMsg());
        assertTrue(res);
    }

        /*
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
                    e.printStackTrace();
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
                e.printStackTrace();
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
    */

}
