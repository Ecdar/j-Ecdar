package parser;

import QueryGrammar.QueryGrammarParser;
import QueryGrammar.QueryGrammarBaseVisitor;
import QueryGrammar.QueryGrammarLexer;
import logic.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    private static List<SimpleTransitionSystem> transitionSystems;

    public static List<Query> parse(String queryString, List<SimpleTransitionSystem> systems){
        transitionSystems = systems;
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

            SystemVisitor systemVisitor = new SystemVisitor();
            return builder
                    .queryType(Query.QueryType.REFINEMENT)
                    .system1(systemVisitor.visit(ctx.system(0)))
                    .system2(systemVisitor.visit(ctx.system(1)));
        }


        @Override
        public Query.QueryBuilder visitSaveSystem(QueryGrammarParser.SaveSystemContext ctx) {

            Query.QueryBuilder builder = new Query.QueryBuilder();

            if(ctx.VARIABLE() != null){
                builder.componentName(ctx.VARIABLE().getText());
            }

            SystemVisitor systemVisitor = new SystemVisitor();
            return builder.system1(systemVisitor.visit(ctx.system()));
        }
    }

    public static class SystemVisitor extends QueryGrammarBaseVisitor<TransitionSystem>{

        private TransitionSystem findComponent(String name){
            for (SimpleTransitionSystem ts : transitionSystems){
                if (ts.getName().equalsIgnoreCase(name))
                    return ts;
            }
            throw new RuntimeException("Automaton does not exist  " + name);
        }

        @Override
        public TransitionSystem visitSystem(QueryGrammarParser.SystemContext ctx) {
            if(ctx.system().size() == 2){
                TransitionSystem system1 = visit(ctx.system(0));
                TransitionSystem system2 = visit(ctx.system(1));

                if(ctx.CONJUNCTION() != null){
                    return new Conjunction(new TransitionSystem[]{system1, system2});
                }else if(ctx.COMPOSITION() != null){
                    return new Composition(new TransitionSystem[]{system1, system2});
                }else if(ctx.QUOTIENT() != null){
                    return new Quotient(system1, system2); // TODO: Check if correct
                }else {
                    throw new RuntimeException("Expected composition, conjunction or quotient");
                }
            }

            if(ctx.system().size() == 1){
                return visit(ctx.system(0));
            } else {
               return findComponent(ctx.VARIABLE().getText());
            }
        }
    }
}
