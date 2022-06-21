package parser;

import GuardGrammar.GuardGrammarParser;
import GuardGrammar.GuardGrammarLexer;
import GuardGrammar.GuardGrammarBaseVisitor;
import exceptions.BooleanVariableNotFoundException;
import exceptions.ClockNotFoundException;
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

    private static Clock findClock(String clockName) {
        for (Clock clock : clocks)
            if (clock.getOriginalName().equals(clockName)) return clock;

        throw new ClockNotFoundException("Clock: " + clockName + " was not found");
    }

    private static BoolVar findBV(String name) {
        for (BoolVar bv : BVs)
            if (bv.getName().equals(name))
                return bv;

        throw new BooleanVariableNotFoundException("Boolean variable: " + name + " was not found");
    }

    public static Guard parse(String guardString, List<Clock> clockList, List<BoolVar> BVList) {
        clocks = clockList;
        BVs = BVList;
        CharStream charStream = CharStreams.fromString(guardString);
        GuardGrammarLexer lexer = new GuardGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);
        GuardGrammarParser parser = new GuardGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        GuardVisitor guardVisitor = new GuardVisitor();
        return guardVisitor.visit(parser.guard());
    }

    private static class GuardVisitor extends  GuardGrammarBaseVisitor<Guard>{

        @Override
        public Guard visitOr(GuardGrammarParser.OrContext ctx) {
            List<Guard> orGuards = new ArrayList<>();
            for(GuardGrammarParser.OrExpressionContext orExpression: ctx.orExpression()){
                orGuards.add(visit(orExpression));
            }

            return new OrGuard(orGuards);
        }

        public Guard visitAnd(GuardGrammarParser.AndContext ctx) {
            List<Guard> guards = new ArrayList<>();
            for (GuardGrammarParser.ExpressionContext expression: ctx.expression()) {
                guards.add(visit(expression));
            }
            return new AndGuard(guards);
        }

        @Override
        public Guard visitExpression(GuardGrammarParser.ExpressionContext ctx) {
            if(ctx.BOOLEAN() != null) {
                boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
                return value ? new TrueGuard() : new FalseGuard();
            }else if(ctx.guard() != null){
                return visit(ctx.guard());
            }
            return visitChildren(ctx);
        }

        @Override
        public Guard visitClockExpr(GuardGrammarParser.ClockExprContext ctx) {
            int value = Integer.parseInt(ctx.INT().getText());
            String operator = ctx.OPERATOR().getText();
            Clock clock = findClock(ctx.VARIABLE().getText());

            Relation relation = Relation.fromString(operator);
            return new ClockGuard(clock, value, relation);
        }

        @Override
        public Guard visitBoolExpr(GuardGrammarParser.BoolExprContext ctx) {
            boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
            String operator = ctx.OPERATOR().getText();
            BoolVar bv = findBV(ctx.VARIABLE().getText());

            return new BoolGuard(bv, operator, value);
        }
    }
}
