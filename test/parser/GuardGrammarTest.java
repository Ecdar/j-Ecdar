package parser;

import GuardGrammar.GuardGrammarLexer;
import GuardGrammar.GuardGrammarParser;
import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GuardGrammarTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        GuardGrammar.GuardGrammarLexer lexer = new GuardGrammar.GuardGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private GuardGrammar.GuardGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        GuardGrammar.GuardGrammarParser parser = new GuardGrammar.GuardGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testLexerSimpleCompare(){
        List<Token> tokens = getTokensFromText("x>=5");

        assertEquals(4, tokens.size());
        assertEquals(GuardGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(GuardGrammar.GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.INT, tokens.get(2).getType());
    }

    @Test
    public void testLexerWithAnd(){
        List<Token> tokens = getTokensFromText("x<=5 && y==3");

        assertEquals(8, tokens.size());
        assertEquals(GuardGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(GuardGrammar.GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.INT, tokens.get(2).getType());

        assertEquals(GuardGrammarLexer.VARIABLE, tokens.get(4).getType());
        assertEquals(GuardGrammar.GuardGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(GuardGrammarLexer.INT, tokens.get(6).getType());
    }

    @Test
    public void testLexerWithOr(){
        List<Token> tokens = getTokensFromText("x<=5 || y==3");

        assertEquals(8, tokens.size());
        assertEquals(GuardGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(GuardGrammar.GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.INT, tokens.get(2).getType());

        assertEquals(GuardGrammarLexer.VARIABLE, tokens.get(4).getType());
        assertEquals(GuardGrammar.GuardGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(GuardGrammarLexer.INT, tokens.get(6).getType());
    }

    @Test
    public void testParsing(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4"));

        GuardGrammar.GuardGrammarParser.ClockExprContext ctx = parser.guard().expression().clockExpr();
        assertEquals("x", ctx.VARIABLE(0).getText());
        assertEquals("4", ctx.INT().getText());
        assertEquals("<", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingOuterAnd(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<=1 || (y<=2 || x<=3) && (y<=2 || x<=3) || y>6"));

        GuardGrammarParser.GuardContext guardContext = parser.guard();
        GuardGrammar.GuardGrammarParser.ClockExprContext ctx = guardContext.or().orExpression(1).and().expression(0).guard().or().orExpression(1).expression().clockExpr();
        assertEquals("x", ctx.VARIABLE(0).getText());
        assertEquals("3", ctx.INT().getText());
        assertEquals("<=", ctx.OPERATOR().getText());

        GuardGrammar.GuardGrammarParser.ClockExprContext ctx1 = guardContext.or().orExpression(1).and().expression(1).guard().or().orExpression(0).expression().clockExpr();
        assertEquals("y", ctx1.VARIABLE(0).getText());
        assertEquals("2", ctx1.INT().getText());
        assertEquals("<=", ctx1.OPERATOR().getText());
    }

    @Test
    public void testParsingWithOr(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4||y>=5"));

        GuardGrammar.GuardGrammarParser.ClockExprContext ctx = parser.guard().or().orExpression(1).expression().clockExpr();
        assertEquals("y", ctx.VARIABLE(0).getText());
        assertEquals("5", ctx.INT().getText());
        assertEquals(">=", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingSymbolAndTextAnd(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4 AND y>=5 and y>6 && y<7"));

        GuardGrammarParser.AndContext andContext = parser.guard().and();

        GuardGrammarParser.ClockExprContext clock1 = andContext.expression(0).clockExpr();
        assertEquals("x", clock1.VARIABLE(0).getText());
        assertEquals("4", clock1.INT().getText());
        assertEquals("<", clock1.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock2 = andContext.expression(1).clockExpr();
        assertEquals("y", clock2.VARIABLE(0).getText());
        assertEquals("5", clock2.INT().getText());
        assertEquals(">=", clock2.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock3 = andContext.expression(2).clockExpr();
        assertEquals("y", clock3.VARIABLE(0).getText());
        assertEquals("6", clock3.INT().getText());
        assertEquals(">", clock3.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock4 = andContext.expression(3).clockExpr();
        assertEquals("y", clock4.VARIABLE(0).getText());
        assertEquals("7", clock4.INT().getText());
        assertEquals("<", clock4.OPERATOR().getText());
    }

    @Test
    public void testParsingSymbolAndTextOr(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4 OR y>=5 or y>6 || y<7"));

        GuardGrammarParser.OrContext orContext = parser.guard().or();

        GuardGrammarParser.ClockExprContext clock1 = orContext.orExpression(0).expression().clockExpr();
        assertEquals("x", clock1.VARIABLE(0).getText());
        assertEquals("4", clock1.INT().getText());
        assertEquals("<", clock1.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock2 = orContext.orExpression(1).expression().clockExpr();
        assertEquals("y", clock2.VARIABLE(0).getText());
        assertEquals("5", clock2.INT().getText());
        assertEquals(">=", clock2.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock3 = orContext.orExpression(2).expression().clockExpr();
        assertEquals("y", clock3.VARIABLE(0).getText());
        assertEquals("6", clock3.INT().getText());
        assertEquals(">", clock3.OPERATOR().getText());

        GuardGrammarParser.ClockExprContext clock4 = orContext.orExpression(3).expression().clockExpr();
        assertEquals("y", clock4.VARIABLE(0).getText());
        assertEquals("7", clock4.INT().getText());
        assertEquals("<", clock4.OPERATOR().getText());
    }
}
