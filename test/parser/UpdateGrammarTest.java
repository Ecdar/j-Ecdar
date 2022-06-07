package parser;

import UpdateGrammar.UpdateGrammarLexer;
import UpdateGrammar.UpdateGrammarParser;
import org.antlr.v4.runtime.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class UpdateGrammarTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        UpdateGrammar.UpdateGrammarLexer lexer = new UpdateGrammar.UpdateGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private UpdateGrammar.UpdateGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        UpdateGrammar.UpdateGrammarParser parser = new UpdateGrammar.UpdateGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testLexerMultiple(){
        List<Token> tokens = getTokensFromText("x=4, y=3");

        assertEquals(8, tokens.size());
        assertEquals(UpdateGrammar.UpdateGrammarLexer.VARIABLE, tokens.get(0).getType());
        assertEquals(UpdateGrammar.UpdateGrammarLexer.INT, tokens.get(2).getType());

        assertEquals(UpdateGrammar.UpdateGrammarLexer.VARIABLE, tokens.get(4).getType());
        assertEquals(UpdateGrammar.UpdateGrammarLexer.INT, tokens.get(6).getType());
    }

    @Test
    public void testParsingUpdate(){
        UpdateGrammar.UpdateGrammarParser parser = createParserNoError(getTokensFromText("x = 0"));

        UpdateGrammar.UpdateGrammarParser.ClockAssignmentContext ctx = parser.update().assignment(0).clockAssignment();
        assertEquals("x", ctx.VARIABLE().getText());
        assertEquals("0", ctx.INT().getText());
    }

    @Test
    public void testParsingUpdateMultiple(){
        UpdateGrammar.UpdateGrammarParser parser = createParserNoError(getTokensFromText("x=4, y=3"));

        UpdateGrammarParser.UpdateContext updateContext = parser.update();
        UpdateGrammar.UpdateGrammarParser.ClockAssignmentContext clockAssignment1 = updateContext.assignment(0).clockAssignment();
        assertEquals("x", clockAssignment1.VARIABLE().getText());
        assertEquals("4", clockAssignment1.INT().getText());

        UpdateGrammar.UpdateGrammarParser.ClockAssignmentContext clockAssignment2 = updateContext.assignment(1).clockAssignment();
        assertEquals("y", clockAssignment2.VARIABLE().getText());
        assertEquals("3", clockAssignment2.INT().getText());
    }
}
