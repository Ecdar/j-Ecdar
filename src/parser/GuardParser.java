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

        GuardVisitor guardVisitor = new GuardVisitor();
        return guardVisitor.visit(parser.guard());
    }

    private static class GuardVisitor extends  EdgeGrammarBaseVisitor<Guard>{
        @Override
        public Guard visitGuard(EdgeGrammarParser.GuardContext ctx) {
            if(ctx.or() != null){
                OrVisitor orVisitor = new OrVisitor();
                return orVisitor.visit(ctx.or());
            }else if(ctx.and() != null){
                AndVisitor andVisitor = new AndVisitor();
                return andVisitor.visit(ctx.and());
            } else if(ctx.expression() != null){
                ExpressionVisitor expressionVisitor = new ExpressionVisitor();
                return expressionVisitor.visit(ctx.expression());
            }else{
                return null;
            }
        }
    }

    private static class OrVisitor extends EdgeGrammarBaseVisitor<Guard>{

        @Override
        public Guard visitOr(EdgeGrammarParser.OrContext ctx) {
            List<Guard> orGuards = new ArrayList<>();
            for(EdgeGrammarParser.OrExpressionContext orExpression: ctx.orExpression()){
                orGuards.add(visit(orExpression));
            }

            return new OrGuard(orGuards);
        }

        @Override
        public Guard visitOrExpression(EdgeGrammarParser.OrExpressionContext ctx) {
            if(ctx.expression() != null){
                ExpressionVisitor expressionVisitor = new ExpressionVisitor();
                return expressionVisitor.visit(ctx.expression());
            }else if(ctx.and() != null){
                AndVisitor andVisitor = new AndVisitor();
                return new AndGuard(andVisitor.visit(ctx.and()));
            }else{
                throw new RuntimeException("Unexpected context");
            }
        }
    }

    private static class AndVisitor extends EdgeGrammarBaseVisitor<Guard>{

        @Override
        public Guard visitAnd(EdgeGrammarParser.AndContext ctx) {
            List<Guard> guards = new ArrayList<>();
            ExpressionVisitor expressionVisitor = new ExpressionVisitor();
            for (EdgeGrammarParser.ExpressionContext expression: ctx.expression()) {
                guards.add(expressionVisitor.visit(expression));
            }
            return new AndGuard(guards);
        }
    }

    private static class ExpressionVisitor extends EdgeGrammarBaseVisitor<Guard> {
         private static Clock findClock(String clockName) {
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
             }else if(ctx.guard() != null){
                 GuardVisitor guardVisitor = new GuardVisitor();
                 return guardVisitor.visit(ctx.guard());
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
