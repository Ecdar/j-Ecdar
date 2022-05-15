package parser;

import GuardGrammar.GuardGrammarParser;
import models.Clock;
import models.Guard;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class GuardParser {

    private List<Clock> clocks;

    public GuardParser(List<Clock> clock) {
        this.clocks = clock;
    }

    public List<List<Guard>> parse(String guardString){
        CharStream charStream = CharStreams.fromString(guardString);
        GuardGrammar.GuardGrammarLexer lexer = new GuardGrammar.GuardGrammarLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        GuardGrammarParser parser = new GuardGrammarParser(tokens);

        OrVisitor orVisitor = new OrVisitor();
        return orVisitor.visit(parser.guard());
    }

    private class OrVisitor extends GuardGrammar.GuardGrammarBaseVisitor<List<List<Guard>>>{

        @Override
        public List<List<Guard>> visitGuard(GuardGrammarParser.GuardContext ctx) {
            if(ctx.or() != null){
                return visit(ctx.or());
            }else{
                return null;
            }
        }

        @Override
        public List<List<Guard>> visitOr(GuardGrammarParser.OrContext ctx) {
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

    private class AndVisitor extends GuardGrammar.GuardGrammarBaseVisitor<List<Guard>>{
        @Override
        public List<Guard> visitAnd(GuardGrammarParser.AndContext ctx) {
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

    private class ExpressionVisitor extends GuardGrammar.GuardGrammarBaseVisitor<Guard> {
         private Clock findClock(String clockName) {
            for (Clock clock : GuardParser.this.clocks)
                if (clock.getName().equals(clockName)) return clock;

            return null;
        }

        @Override
        public Guard visitCompareExpr(GuardGrammarParser.CompareExprContext ctx) {
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
