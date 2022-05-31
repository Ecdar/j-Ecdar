package parser;

import org.antlr.v4.runtime.*;
import QueryGrammar.QueryGrammarLexer;
import QueryGrammar.QueryGrammarParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryGrammarTest {

    private List<Token> getTokensFromText(String txt){
        CharStream charStream = CharStreams.fromString(txt);
        QueryGrammarLexer lexer = new QueryGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        return tokenStream.getTokens();
    }

    private QueryGrammarParser createParserNoError(List<Token> tokens){
        ListTokenSource tokenSource = new ListTokenSource(tokens);
        CommonTokenStream tokenStream = new CommonTokenStream(tokenSource);
        QueryGrammarParser parser = new QueryGrammarParser(tokenStream);
        parser.addErrorListener(new NoErrorListener());
        return parser;
    }

    @Test
    public void testLexerSingleQuery(){
        List<Token> tokens = getTokensFromText("get-component: A && F");

        assertEquals(6, tokens.size());
        assertEquals(QueryGrammarLexer.QUERY_TYPE, tokens.get(0).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(2).getType());
        assertEquals(QueryGrammarLexer.CONJUNCTION, tokens.get(3).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(4).getType());
    }

    @Test
    public void testLexerQuotient(){
        List<Token> tokens = getTokensFromText("refinement: A <= B \\\\ (A || Q)");

        assertEquals(11, tokens.size());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(1).getType());
        assertEquals(QueryGrammarLexer.QUOTIENT, tokens.get(4).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(6).getType());
        assertEquals(QueryGrammarLexer.COMPOSITION, tokens.get(7).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(8).getType());
    }

    @Test
    public void testLexerMultipleQueries(){
        List<Token> tokens = getTokensFromText("refinement: A <= B; get-component: C");

        assertEquals(9, tokens.size());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(1).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(3).getType());
        assertEquals(QueryGrammarLexer.QUERY_TYPE, tokens.get(5).getType());
        assertEquals(QueryGrammarLexer.VARIABLE, tokens.get(7).getType());
    }

    @Test
    public void testParsingSingleQuery(){
        QueryGrammarParser parser = createParserNoError(getTokensFromText("get-component: A && B || F"));

        QueryGrammarParser.QueryContext ctx = parser.queries().query(0);
        assertEquals("get-component", ctx.QUERY_TYPE().getText());
        assertEquals("A", ctx.saveSystem().system().system(0).system(0).VARIABLE().getText());
        assertEquals("||", ctx.saveSystem().system().COMPOSITION().getText());
        assertEquals("F", ctx.saveSystem().system().system(1).VARIABLE().getText());
    }

    @Test
    public void testParsingQuotient(){
        QueryGrammarParser parser = createParserNoError(getTokensFromText("refinement: A <= B \\\\ (A || Q)"));

        QueryGrammarParser.QueryContext ctx = parser.queries().query(0);
        assertEquals("A", ctx.refinement().system(0).VARIABLE().getText());
        assertEquals("\\\\", ctx.refinement().system(1).QUOTIENT().getText());
        assertEquals("B", ctx.refinement().system(1).system(0).getText());
    }

    @Test
    public void testParsingMultipleQueries(){
        QueryGrammarParser parser = createParserNoError(getTokensFromText("refinement: A <= B; get-component: C save-as D"));

        QueryGrammarParser.QueriesContext queriesContext = parser.queries();
        QueryGrammarParser.RefinementContext refinementCtx =queriesContext.query(0).refinement();
        QueryGrammarParser.QueryContext getComponentCtx = queriesContext.query(1);
        assertEquals("A", refinementCtx.system(0).VARIABLE().getText());
        assertEquals("B", refinementCtx.system(1).VARIABLE().getText());
        assertEquals("C", getComponentCtx.saveSystem().system().VARIABLE().getText());
        assertEquals("D", getComponentCtx.saveSystem().VARIABLE().getText());
    }
}
