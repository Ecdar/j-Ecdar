package parser;

import EdgeGrammar.EdgeGrammarLexer;
import EdgeGrammar.EdgeGrammarParser;
import EdgeGrammar.EdgeGrammarBaseVisitor;
import models.BoolVar;
import models.Clock;
import models.ClockUpdate;
import models.Update;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class UpdateParser {
    private static List<Clock> clocks;
    private static List<BoolVar> BVs;

    public static List<Update> parse(String updateString, List<Clock> clockList, List<BoolVar> BVList){
        clocks = clockList;
        BVs = BVList;
        CharStream charStream = CharStreams.fromString(updateString);
        EdgeGrammar.EdgeGrammarLexer lexer = new EdgeGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);
        EdgeGrammar.EdgeGrammarParser parser = new EdgeGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        UpdatesVisitor updatesVisitor = new UpdatesVisitor();
        return updatesVisitor.visit(parser.update());
    }

    private static class UpdatesVisitor extends EdgeGrammarBaseVisitor<List<Update>>{
        private List<Update> updates;

        public UpdatesVisitor() {
            updates = new ArrayList<>();
        }

        @Override
        public List<Update> visitUpdate(EdgeGrammarParser.UpdateContext ctx) {
            if(ctx.assignments() != null){
                return visit(ctx.assignments());
            }else{
                return null;
            }
        }

        @Override
        public List<Update> visitAssignments(EdgeGrammarParser.AssignmentsContext ctx) {
            AssignmentVisitor assignmentVisitor = new AssignmentVisitor();
            updates.add(assignmentVisitor.visit(ctx.assignment()));

            if(ctx.assignments() != null)
                updates = visit(ctx.assignments());

            return updates;
        }
    }

    private static class AssignmentVisitor extends EdgeGrammarBaseVisitor<Update>{
        private Clock findClock(String clockName) {
            for (Clock clock : clocks)
                if (clock.getName().equals(clockName)) return clock;

            return null;
        }

        private static BoolVar findBV(List<BoolVar> BVs, String name) {
            for (BoolVar bv : BVs)
                if (bv.getName().equals(name))
                    return bv;

            return null;
        }

        @Override
        public Update visitAssignment(EdgeGrammarParser.AssignmentContext ctx) {
            Clock clock = findClock(ctx.TERM(0).getText());

            return new ClockUpdate(clock, Integer.parseInt(ctx.TERM(1).getText()));
        }
    }
}
