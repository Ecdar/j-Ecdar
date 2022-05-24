package parser;

import QueryGrammar.QueryGrammarParser;
import QueryGrammar.QueryGrammarBaseVisitor;
import QueryGrammar.QueryGrammarLexer;
import logic.Query;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    public static List<Query> parse(String queryString){
        CharStream charStream = CharStreams.fromString(queryString);
        QueryGrammarLexer lexer = new QueryGrammarLexer(charStream);
        lexer.addErrorListener(new ErrorListener());
        TokenStream tokens = new CommonTokenStream(lexer);

        QueryGrammarParser parser = new QueryGrammarParser(tokens);
        parser.addErrorListener(new ErrorListener());

        QueriesVisitor queriesVisitor = new QueriesVisitor();
        return queriesVisitor.visit(parser.queries());
    }

    private static class QueriesVisitor extends QueryGrammarBaseVisitor<List<Query>>{
        @Override
        public List<Query> visitQueries(QueryGrammarParser.QueriesContext ctx) {
            QueryVisitor queryVisitor = new QueryVisitor();
            List<Query> queries = new ArrayList<>();
            for (QueryGrammarParser.QueryContext queryCtx: ctx.query()){
                queries.add(queryVisitor.visitQuery(queryCtx).build());
            }
            return queries;
        }
    }

    private static class QueryVisitor extends QueryGrammarBaseVisitor<Query.QueryBuilder> {
        @Override
        public Query.QueryBuilder visitQuery(QueryGrammarParser.QueryContext ctx) {

            if (ctx.refinement() != null) {
                return visit(ctx.refinement());
            }

            Query.QueryBuilder builder = visit(ctx.saveSystem());

            switch (ctx.QUERY_TYPE().getText()) {
                case "get-component":
                    builder.queryType(Query.QueryType.GET_COMPONENT);
                    break;
                case "consistency":
                    builder.queryType(Query.QueryType.CONSISTENCY);
                    break;
                case "implementation":
                    builder.queryType(Query.QueryType.IMPLEMENTATION);
                    break;
                case "determinism":
                    builder.queryType(Query.QueryType.DETERMINISM);
                    break;
                case "bisim-minim":
                    builder.queryType(Query.QueryType.BISIM_MINIM);
                    break;
                case "prune":
                    builder.queryType(Query.QueryType.PRUNE);
                    break;
            }

            return builder;
        }

        @Override
        public Query.QueryBuilder visitRefinement(QueryGrammarParser.RefinementContext ctx) {

            Query.QueryBuilder builder = new Query.QueryBuilder();

            return builder
                    .queryType(Query.QueryType.REFINEMENT)
                    .system1(ctx.system(0).getText())
                    .system2(ctx.system(1).getText());
        }


        @Override
        public Query.QueryBuilder visitSaveSystem(QueryGrammarParser.SaveSystemContext ctx) {

            Query.QueryBuilder builder = new Query.QueryBuilder();

            if(ctx.VARIABLE() != null){
                builder.componentName(ctx.VARIABLE().getText());
            }

            return builder.system1(ctx.system().getText());
        }
    }
}
