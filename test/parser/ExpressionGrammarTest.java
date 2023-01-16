package parser;

import ExpressionGrammar.ExpressionGrammarLexer;
import ExpressionGrammar.ExpressionGrammarParser;
import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExpressionGrammarTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        ExpressionGrammarLexer lexer = new ExpressionGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private ExpressionGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        ExpressionGrammarParser parser = new ExpressionGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testLexerSimpleCompare(){
        List<Token> tokens = getTokensFromText("x>=5");

        assertEquals(4, tokens.size());
        assertEquals(ExpressionGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(ExpressionGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(ExpressionGrammarLexer.INT, tokens.get(2).getType());
    }

    @Test
    public void testLexerWithAnd(){
        List<Token> tokens = getTokensFromText("x<=5 && y==3");

        assertEquals(8, tokens.size());
        assertEquals(ExpressionGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(ExpressionGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(ExpressionGrammarLexer.INT, tokens.get(2).getType());

        assertEquals(ExpressionGrammarLexer.VARIABLE, tokens.get(4).getType());
        assertEquals(ExpressionGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(ExpressionGrammarLexer.INT, tokens.get(6).getType());
    }

    @Test
    public void testLexerWithOr(){
        List<Token> tokens = getTokensFromText("x<=5 || y==3");

        assertEquals(8, tokens.size());
        assertEquals(ExpressionGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(ExpressionGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(ExpressionGrammarLexer.INT, tokens.get(2).getType());

        assertEquals(ExpressionGrammarLexer.VARIABLE, tokens.get(4).getType());
        assertEquals(ExpressionGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(ExpressionGrammarLexer.INT, tokens.get(6).getType());
    }

    @Test
    public void testParsing(){
        ExpressionGrammarParser parser = createParserNoError(getTokensFromText("x<4"));

        ExpressionGrammarParser.ClockExprContext ctx = parser.expression().arithExpression().clockExpr();
        assertEquals("x", ctx.VARIABLE(0).getText());
        assertEquals("4", ctx.INT().getText());
        assertEquals("<", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingOuterAnd(){
        ExpressionGrammarParser parser = createParserNoError(getTokensFromText("x<=1 || (y<=2 || x<=3) && (y<=2 || x<=3) || y>6"));

        ExpressionGrammarParser.ExpressionContext expressionContext = parser.expression();
        ExpressionGrammarParser.ClockExprContext ctx = expressionContext.or().orExpression(1).and().arithExpression(0).expression().or().orExpression(1).arithExpression().clockExpr();
        assertEquals("x", ctx.VARIABLE(0).getText());
        assertEquals("3", ctx.INT().getText());
        assertEquals("<=", ctx.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext ctx1 = expressionContext.or().orExpression(1).and().arithExpression(1).expression().or().orExpression(0).arithExpression().clockExpr();
        assertEquals("y", ctx1.VARIABLE(0).getText());
        assertEquals("2", ctx1.INT().getText());
        assertEquals("<=", ctx1.OPERATOR().getText());
    }

    @Test
    public void testParsingWithOr(){
        ExpressionGrammarParser parser = createParserNoError(getTokensFromText("x<4||y>=5"));

        ExpressionGrammarParser.ClockExprContext ctx = parser.expression().or().orExpression(1).arithExpression().clockExpr();
        assertEquals("y", ctx.VARIABLE(0).getText());
        assertEquals("5", ctx.INT().getText());
        assertEquals(">=", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingSymbolAndTextAnd(){
        ExpressionGrammarParser parser = createParserNoError(getTokensFromText("x<4 AND y>=5 and y>6 && y<7"));

        ExpressionGrammarParser.AndContext andContext = parser.expression().and();

        ExpressionGrammarParser.ClockExprContext clock1 = andContext.arithExpression(0).clockExpr();
        assertEquals("x", clock1.VARIABLE(0).getText());
        assertEquals("4", clock1.INT().getText());
        assertEquals("<", clock1.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock2 = andContext.arithExpression(1).clockExpr();
        assertEquals("y", clock2.VARIABLE(0).getText());
        assertEquals("5", clock2.INT().getText());
        assertEquals(">=", clock2.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock3 = andContext.arithExpression(2).clockExpr();
        assertEquals("y", clock3.VARIABLE(0).getText());
        assertEquals("6", clock3.INT().getText());
        assertEquals(">", clock3.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock4 = andContext.arithExpression(3).clockExpr();
        assertEquals("y", clock4.VARIABLE(0).getText());
        assertEquals("7", clock4.INT().getText());
        assertEquals("<", clock4.OPERATOR().getText());
    }

    @Test
    public void testParsingSymbolAndTextOr(){
        ExpressionGrammarParser parser = createParserNoError(getTokensFromText("x<4 OR y>=5 or y>6 || y<7"));

        ExpressionGrammarParser.OrContext orContext = parser.expression().or();

        ExpressionGrammarParser.ClockExprContext clock1 = orContext.orExpression(0).arithExpression().clockExpr();
        assertEquals("x", clock1.VARIABLE(0).getText());
        assertEquals("4", clock1.INT().getText());
        assertEquals("<", clock1.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock2 = orContext.orExpression(1).arithExpression().clockExpr();
        assertEquals("y", clock2.VARIABLE(0).getText());
        assertEquals("5", clock2.INT().getText());
        assertEquals(">=", clock2.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock3 = orContext.orExpression(2).arithExpression().clockExpr();
        assertEquals("y", clock3.VARIABLE(0).getText());
        assertEquals("6", clock3.INT().getText());
        assertEquals(">", clock3.OPERATOR().getText());

        ExpressionGrammarParser.ClockExprContext clock4 = orContext.orExpression(3).arithExpression().clockExpr();
        assertEquals("y", clock4.VARIABLE(0).getText());
        assertEquals("7", clock4.INT().getText());
        assertEquals("<", clock4.OPERATOR().getText());
    }
}
