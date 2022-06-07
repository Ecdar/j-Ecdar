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
        assertEquals("x", ctx.VARIABLE().getText());
        assertEquals("4", ctx.INT().getText());
        assertEquals("<", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingOuterAnd(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<=1 || (y<=2 || x<=3) && (y<=2 || x<=3) || y>6"));

        GuardGrammarParser.GuardContext guardContext = parser.guard();
        GuardGrammar.GuardGrammarParser.ClockExprContext ctx = guardContext.or().orExpression(1).and().expression(0).guard().or().orExpression(1).expression().clockExpr();
        assertEquals("x", ctx.VARIABLE().getText());
        assertEquals("3", ctx.INT().getText());
        assertEquals("<=", ctx.OPERATOR().getText());

        GuardGrammar.GuardGrammarParser.ClockExprContext ctx1 = guardContext.or().orExpression(1).and().expression(1).guard().or().orExpression(0).expression().clockExpr();
        assertEquals("y", ctx1.VARIABLE().getText());
        assertEquals("2", ctx1.INT().getText());
        assertEquals("<=", ctx1.OPERATOR().getText());
    }

    @Test
    public void testParsingWithOr(){
        GuardGrammar.GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4||y>=5"));

        GuardGrammar.GuardGrammarParser.ClockExprContext ctx = parser.guard().or().orExpression(1).expression().clockExpr();
        assertEquals("y", ctx.VARIABLE().getText());
        assertEquals("5", ctx.INT().getText());
        assertEquals(">=", ctx.OPERATOR().getText());
    }
}
