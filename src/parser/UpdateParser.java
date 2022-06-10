package parser;

import UpdateGrammar.UpdateGrammarLexer;
import UpdateGrammar.UpdateGrammarParser;
import UpdateGrammar.UpdateGrammarBaseVisitor;
import models.*;
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
        UpdateGrammar.UpdateGrammarLexer lexer = new UpdateGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);
        UpdateGrammar.UpdateGrammarParser parser = new UpdateGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        UpdatesVisitor updatesVisitor = new UpdatesVisitor();
        return updatesVisitor.visit(parser.update());
    }

    private static class UpdatesVisitor extends UpdateGrammarBaseVisitor<List<Update>>{
        private List<Update> updates;

        public UpdatesVisitor() {
            updates = new ArrayList<>();
        }

        @Override
        public List<Update> visitUpdate(UpdateGrammarParser.UpdateContext ctx) {
            AssignmentVisitor assignmentVisitor = new AssignmentVisitor();
            for(UpdateGrammarParser.AssignmentContext assignment : ctx.assignment()){
                updates.add(assignmentVisitor.visit(assignment));
            }
            return updates;
        }
    }

    private static class AssignmentVisitor extends UpdateGrammarBaseVisitor<Update>{
        private Clock findClock(String clockName) {
            for (Clock clock : clocks)
                if (clock.getName().equals(clockName)) return clock;

            return null;
        }

        private static BoolVar findBV(String name) {
            for (BoolVar bv : BVs)
                if (bv.getName().equals(name))
                    return bv;

            return null;
        }

        @Override
        public Update visitAssignment(UpdateGrammarParser.AssignmentContext ctx) {
            return visitChildren(ctx);
        }

        @Override
        public Update visitClockAssignment(UpdateGrammarParser.ClockAssignmentContext ctx) {
            Clock clock = findClock(ctx.VARIABLE().getText());

            return new ClockUpdate(clock, Integer.parseInt(ctx.INT().getText()));
        }

        @Override
        public Update visitBoolAssignment(UpdateGrammarParser.BoolAssignmentContext ctx) {
            BoolVar bv = findBV(ctx.VARIABLE().getText());

            return new BoolUpdate(bv, Boolean.parseBoolean(ctx.BOOLEAN().getText()));
        }
    }
}
