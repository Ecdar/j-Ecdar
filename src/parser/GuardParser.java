package parser;

import EdgeGrammar.EdgeGrammarParser;
import EdgeGrammar.EdgeGrammarLexer;
import EdgeGrammar.EdgeGrammarBaseVisitor;
import models.Clock;
import models.Guard;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class GuardParser {

    private static List<Clock> clocks;

    public static List<List<Guard>> parse(String guardString, List<Clock> clockList){
        clocks = clockList;
        CharStream charStream = CharStreams.fromString(guardString);
        EdgeGrammarLexer lexer = new EdgeGrammarLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        EdgeGrammarParser parser = new EdgeGrammarParser(tokens);

        OrVisitor orVisitor = new OrVisitor();
        return orVisitor.visit(parser.guard());
    }

    private static class OrVisitor extends EdgeGrammarBaseVisitor<List<List<Guard>>>{

        @Override
        public List<List<Guard>> visitGuard(EdgeGrammarParser.GuardContext ctx) {
            if(ctx.or() != null){
                return visit(ctx.or());
            }else{
                return null;
            }
        }

        @Override
        public List<List<Guard>> visitOr(EdgeGrammarParser.OrContext ctx) {
            List<List<Guard>> guardList;
            if(ctx.or() != null){
                guardList = visit(ctx.or());
            }else {
                guardList = new ArrayList<>();
            }

            AndVisitor andVisitor = new AndVisitor();
            guardList.add(andVisitor.visit(ctx.and()));

            return guardList;
        }
    }

    private static class AndVisitor extends EdgeGrammarBaseVisitor<List<Guard>>{
        @Override
        public List<Guard> visitAnd(EdgeGrammarParser.AndContext ctx) {
            List<Guard> guards;
            if(ctx.and() != null){
                guards = visit(ctx.and());
            }else{
                guards = new ArrayList<>();
            }

            ExpressionVisitor expressionVisitor = new ExpressionVisitor();
            Guard guard = expressionVisitor.visit(ctx.compareExpr());
            guards.add(guard);

            return guards;
        }
    }

    private static class ExpressionVisitor extends EdgeGrammarBaseVisitor<Guard> {
         private Clock findClock(String clockName) {
            for (Clock clock : clocks)
                if (clock.getName().equals(clockName)) return clock;

            return null;
        }

        @Override
        public Guard visitCompareExpr(EdgeGrammarParser.CompareExprContext ctx) {
            int value = Integer.parseInt(ctx.TERM(1).getText());
            String operator = ctx.OPERATOR().getText();
            Clock clock = findClock(ctx.TERM(0).getText());
            Guard guard;

            if(operator.equals("==")){
                guard = new Guard(clock, value);
            }else {
                boolean isStrict = false, greater = false;
                if(operator.startsWith(">")){
                    greater = true;
                }
                if(operator.length() == 1){
                    isStrict = true;
                }
                guard = new Guard(clock, value, greater, isStrict);
            }

            return guard;
        }
    }
}
