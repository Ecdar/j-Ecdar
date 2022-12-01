package parser;

import ExpressionGrammar.ExpressionGrammarParser;
import ExpressionGrammar.ExpressionGrammarLexer;
import ExpressionGrammar.ExpressionGrammarBaseVisitor;
import exceptions.BooleanVariableNotFoundException;
import exceptions.ClockNotFoundException;
import models.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {

    private static List<Clock> clocks;
    private static List<BoolVar> BVs;

    private static Clock findClock(String clockName) {
        for (Clock clock : clocks)
            if (clock.getOriginalName().equals(clockName)) return clock;

        throw new ClockNotFoundException("Clock: " + clockName + " was not found");
    }

    private static BoolVar findBV(String name) {
        for (BoolVar bv : BVs)
            if (bv.getOriginalName().equals(name))
                return bv;

        throw new BooleanVariableNotFoundException("Boolean variable: " + name + " was not found");
    }

    public static Expression parse(String expressionString, List<Clock> clockList, List<BoolVar> BVList) {
        clocks = clockList;
        BVs = BVList;
        CharStream charStream = CharStreams.fromString(expressionString);
        ExpressionGrammarLexer lexer = new ExpressionGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);
        ExpressionGrammarParser parser = new ExpressionGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        ExpressionVisitor expressionVisitor = new ExpressionVisitor();
        return expressionVisitor.visit(parser.expression());
    }

    private static class ExpressionVisitor extends  ExpressionGrammarBaseVisitor<Expression>{

        @Override
        public Expression visitOr(ExpressionGrammarParser.OrContext ctx) {
            List<Expression> orExpressions = new ArrayList<>();
            for(ExpressionGrammarParser.OrExpressionContext orExpression: ctx.orExpression()){
                orExpressions.add(visit(orExpression));
            }

            return new OrExpression(orExpressions);
        }

        public Expression visitAnd(ExpressionGrammarParser.AndContext ctx) {
            List<Expression> expressions = new ArrayList<>();
            for (ExpressionGrammarParser.ArithExpressionContext expression: ctx.arithExpression()) {
                expressions.add(visit(expression));
            }
            return new AndExpression(expressions);
        }

        @Override
        public Expression visitArithExpression(ExpressionGrammarParser.ArithExpressionContext ctx) {
            if(ctx.BOOLEAN() != null) {
                boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
                return value ? new TrueExpression() : new FalseExpression();
            }else if(ctx.expression() != null){
                return visit(ctx.expression());
            }
            return visitChildren(ctx);
        }

        @Override
        public Expression visitClockExpr(ExpressionGrammarParser.ClockExprContext ctx) {
            int value = Integer.parseInt(ctx.INT().getText());
            String operator = ctx.OPERATOR().getText();
            Clock clock_i = findClock(ctx.VARIABLE(0).getText());
            Relation relation = Relation.fromString(operator);

            if(ctx.VARIABLE().size() > 1){
                Clock clock_j = findClock(ctx.VARIABLE(1).getText());
                return new ClockExpression(clock_i, clock_j, value, relation);
            }else {
                return new ClockExpression(clock_i, value, relation);
            }
        }

        @Override
        public Expression visitBoolExpr(ExpressionGrammarParser.BoolExprContext ctx) {
            boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
            String operator = ctx.OPERATOR().getText();
            BoolVar bv = findBV(ctx.VARIABLE().getText());

            return new BoolExpression(bv, operator, value);
        }
    }
}
