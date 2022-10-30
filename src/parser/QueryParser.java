package parser;

import QueryGrammar.QueryGrammarParser;
import QueryGrammar.QueryGrammarBaseVisitor;
import QueryGrammar.QueryGrammarLexer;
import logic.*;
import logic.query.Query;
import models.Automaton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    private static List<Automaton> automata;

    public static List<Query> parse(String queryString, List<Automaton> systems){
        automata = systems;
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
                Query query = queryVisitor.visitQuery(queryCtx).build();
                query.handle();
                queries.add(query);
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
                    .system1(systemVisitor.visit(ctx.expression(0)))
                    .system2(systemVisitor.visit(ctx.expression(1)));
        }


        @Override
        public Query.QueryBuilder visitSaveSystem(QueryGrammarParser.SaveSystemContext ctx) {

            Query.QueryBuilder builder = new Query.QueryBuilder();

            if(ctx.VARIABLE() != null){
                builder.componentName(ctx.VARIABLE().getText());
            }

            SystemVisitor systemVisitor = new SystemVisitor();
            return builder.system1(systemVisitor.visit(ctx.expression()));
        }
    }

    public static class SystemVisitor extends QueryGrammarBaseVisitor<TransitionSystem>{

        private TransitionSystem findComponent(String name){
            for (Automaton aut : automata){
                if (aut.getName().equalsIgnoreCase(name))
                    return new SimpleTransitionSystem(aut);
            }
            throw new RuntimeException("Automaton does not exist  " + name);
        }

        @Override
        public TransitionSystem visitConjunction(QueryGrammarParser.ConjunctionContext ctx) {
            int size = ctx.conjunctionExpression().size();
            TransitionSystem[] systems = new TransitionSystem[size];

            for (int i = 0; i < size; i++) {
                systems[i] = visit(ctx.conjunctionExpression(i));
            }

            return new Conjunction(systems);
        }

        @Override
        public TransitionSystem visitComposition(QueryGrammarParser.CompositionContext ctx) {
            int size = ctx.compositionExpression().size();
            TransitionSystem[] systems = new TransitionSystem[size];

            for (int i = 0; i < size; i++) {
                systems[i] = visit(ctx.compositionExpression(i));
            }

            return new Composition(systems);
        }

        @Override
        public TransitionSystem visitQuotient(QueryGrammarParser.QuotientContext ctx) {
            TransitionSystem left = visit(ctx.system(0));
            TransitionSystem right = visit(ctx.system(1));

            return new Quotient(left, right);
        }

        @Override
        public TransitionSystem visitSystem(QueryGrammarParser.SystemContext ctx) {
            if(ctx.expression() != null){
                return visit(ctx.expression());
            }else{
                return findComponent(ctx.VARIABLE().getText());
            }
        }
    }
}
