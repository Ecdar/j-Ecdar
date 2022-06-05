package parser;

import EdgeGrammar.EdgeGrammarParser;
import EdgeGrammar.EdgeGrammarLexer;
import EdgeGrammar.EdgeGrammarBaseVisitor;
import models.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class GuardParser {

    private static List<Clock> clocks;
    private static List<BoolVar> BVs;

    public static Guard parse(String guardString, List<Clock> clockList, List<BoolVar> BVList) {
        clocks = clockList;
        BVs = BVList;
        CharStream charStream = CharStreams.fromString(guardString);
        EdgeGrammarLexer lexer = new EdgeGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);
        EdgeGrammarParser parser = new EdgeGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        OrVisitor orVisitor = new OrVisitor();
        List<Guard> guards = orVisitor.visit(parser.guard());
        if(guards.size() > 1)
            return new OrGuard(guards);
        else
            return guards.get(0);
    }

    private static class OrVisitor extends EdgeGrammarBaseVisitor<List<Guard>>{

        private List<Guard> orGuards;

        public OrVisitor() {
            orGuards = new ArrayList<>();
        }

        @Override
        public List<Guard> visitGuard(EdgeGrammarParser.GuardContext ctx) {
            if(ctx.or() != null){
                return visit(ctx.or());
            }else{
                return null;
            }
        }

        @Override
        public List<Guard> visitOr(EdgeGrammarParser.OrContext ctx) {
            AndVisitor andVisitor = new AndVisitor();
            List<Guard> andGuards = andVisitor.visit(ctx.and());
            if(andGuards.size() > 1)
                orGuards.add(new AndGuard(andGuards));
            else
                orGuards.add(andGuards.get(0));

            if(ctx.or() != null)
                visit(ctx.or());

            return orGuards;
        }
    }

    private static class AndVisitor extends EdgeGrammarBaseVisitor<List<Guard>>{

        private List<Guard> guards;

        public AndVisitor() {
            guards = new ArrayList<>();
        }

        @Override
        public List<Guard> visitAnd(EdgeGrammarParser.AndContext ctx) {
            ExpressionVisitor expressionVisitor = new ExpressionVisitor();
            Guard guard = expressionVisitor.visit(ctx.expression());
            guards.add(guard);

            if(ctx.and() != null)
                visit(ctx.and());

            return guards;
        }
    }

    private static class ExpressionVisitor extends EdgeGrammarBaseVisitor<Guard> {
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
        public Guard visitExpression(EdgeGrammarParser.ExpressionContext ctx) {
             if(ctx.BOOLEAN() != null) {
                 boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
                 return value ? new TrueGuard() : new FalseGuard();
             }
            return visitChildren(ctx);
        }

        @Override
        public Guard visitClockExpr(EdgeGrammarParser.ClockExprContext ctx) {
            int value = Integer.parseInt(ctx.INT().getText());
            String operator = ctx.OPERATOR().getText();
            Clock clock = findClock(ctx.VARIABLE().getText());

            Relation relation = Relation.fromString(operator);
            return new ClockGuard(clock, value, relation);
        }

        @Override
        public Guard visitBoolExpr(EdgeGrammarParser.BoolExprContext ctx) {
            boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
            String operator = ctx.OPERATOR().getText();
            BoolVar bv = findBV(ctx.VARIABLE().getText());

            return new BoolGuard(bv, operator, value);
        }
    }
}
