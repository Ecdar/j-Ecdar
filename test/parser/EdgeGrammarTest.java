package parser;

import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class EdgeGrammarTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        EdgeGrammar.EdgeGrammarLexer lexer = new EdgeGrammar.EdgeGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private EdgeGrammar.EdgeGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        EdgeGrammar.EdgeGrammarParser parser = new EdgeGrammar.EdgeGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testLexerSimpleCompare(){
        List<Token> tokens = getTokensFromText("x>=5");

        assertEquals(4, tokens.size());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(2).getType());
    }

    @Test
    public void testLexerWithAnd(){
        List<Token> tokens = getTokensFromText("x<=5 && y==3");

        assertEquals(8, tokens.size());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(2).getType());

        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(4).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(6).getType());
    }

    @Test
    public void testLexerWithOr(){
        List<Token> tokens = getTokensFromText("x<=5 || y==3");

        assertEquals(8, tokens.size());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(2).getType());

        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(4).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(EdgeGrammar.EdgeGrammarLexer.TERM, tokens.get(6).getType());
    }

    @Test
    public void testParsing(){
        EdgeGrammar.EdgeGrammarParser parser = createParserNoError(getTokensFromText("x<4"));

        EdgeGrammar.EdgeGrammarParser.CompareExprContext ctx = parser.guard().or().and().expression().compareExpr();
        assertEquals("x", ctx.TERM(0).getText());
        assertEquals("4", ctx.TERM(1).getText());
        assertEquals("<", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingWithOr(){
        EdgeGrammar.EdgeGrammarParser parser = createParserNoError(getTokensFromText("x<4||y>=5"));

        EdgeGrammar.EdgeGrammarParser.CompareExprContext ctx = parser.guard().or().or().and().expression().compareExpr();
        assertEquals("y", ctx.TERM(0).getText());
        assertEquals("5", ctx.TERM(1).getText());
        assertEquals(">=", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingUpdate(){
        EdgeGrammar.EdgeGrammarParser parser = createParserNoError(getTokensFromText("x = 0"));

        EdgeGrammar.EdgeGrammarParser.AssignmentContext ctx = parser.update().assignments().assignment();
        assertEquals("x", ctx.TERM(0).getText());
        assertEquals("0", ctx.TERM(1).getText());
    }
}
