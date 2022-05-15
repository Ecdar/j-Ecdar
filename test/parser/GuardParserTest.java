package parser;

import GuardGrammar.GuardGrammarLexer;
import GuardGrammar.GuardGrammarParser;
import models.Clock;
import models.Guard;
import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GuardParserTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        GuardGrammar.GuardGrammarLexer lexer = new GuardGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private GuardGrammar.GuardGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        GuardGrammarParser parser = new GuardGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testGuardParser(){
        GuardParser guardParser = new GuardParser(new ArrayList<Clock>()
            {{add(new Clock("x"));}}
        );
        List<List<Guard>> guardList = guardParser.parse("x>5");

        Guard guard = guardList.get(0).get(0);
        assertEquals(true, guard.isStrict());
        assertEquals(5, guard.getLowerBound());
    }

    @Test
    public void testGuardParserAnd(){
        GuardParser guardParser = new GuardParser(new ArrayList<Clock>()
        {{  add(new Clock("x"));
            add(new Clock("y"));}}
        );
        List<List<Guard>> guardList = guardParser.parse("x>=5 && y<6");

        Guard guard = guardList.get(0).get(0);
        assertEquals(true, guard.isStrict());
        assertEquals(6, guard.getUpperBound());

        Guard guard1 = guardList.get(0).get(1);
        assertEquals(false, guard1.isStrict());
        assertEquals(5, guard1.getLowerBound());
    }

    @Test
    public void testGuardParserOr(){
        GuardParser guardParser = new GuardParser(new ArrayList<Clock>()
        {{  add(new Clock("x"));
            add(new Clock("y"));}}
        );
        List<List<Guard>> guardList = guardParser.parse("x>2 || y<5");

        Guard guard1 = guardList.get(0).get(0);
        Guard guard2 = guardList.get(1).get(0);

        assertEquals(true, guard1.isStrict());
        assertEquals(5, guard1.getUpperBound());
        assertEquals(true, guard2.isStrict());
        assertEquals(2, guard2.getLowerBound());
    }

    @Test
    public void testLexerSimpleCompare(){
        List<Token> tokens = getTokensFromText("x>=5");

        assertEquals(4, tokens.size());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(2).getType());
    }

    @Test
    public void testLexerWithAnd(){
        List<Token> tokens = getTokensFromText("x<=5 && y==3");

        assertEquals(8, tokens.size());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(2).getType());

        assertEquals(GuardGrammarLexer.TERM, tokens.get(4).getType());
        assertEquals(GuardGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(6).getType());
    }

    @Test
    public void testLexerWithOr(){
        List<Token> tokens = getTokensFromText("x<=5 || y==3");

        assertEquals(8, tokens.size());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(0).getType());
        assertEquals(GuardGrammarLexer.OPERATOR, tokens.get(1).getType());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(2).getType());

        assertEquals(GuardGrammarLexer.TERM, tokens.get(4).getType());
        assertEquals(GuardGrammarLexer.OPERATOR, tokens.get(5).getType());
        assertEquals(GuardGrammarLexer.TERM, tokens.get(6).getType());
    }

    @Test
    public void testParsing(){
        GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4"));

        GuardGrammarParser.CompareExprContext ctx = parser.guard().or().and().compareExpr();
        assertEquals("x", ctx.TERM(0).getText());
        assertEquals("4", ctx.TERM(1).getText());
        assertEquals("<", ctx.OPERATOR().getText());
    }

    @Test
    public void testParsingWithOr(){
        GuardGrammarParser parser = createParserNoError(getTokensFromText("x<4||y>=5"));

        GuardGrammarParser.CompareExprContext ctx = parser.guard().or().or().and().compareExpr();
        assertEquals("y", ctx.TERM(0).getText());
        assertEquals("5", ctx.TERM(1).getText());
        assertEquals(">=", ctx.OPERATOR().getText());
    }
}
