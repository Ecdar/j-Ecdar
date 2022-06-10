package logic.query;

import logic.TransitionSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Query {
    private QueryType queryType;
    private List<String> resultList;
    private boolean result;
    private String componentName;
    private TransitionSystem system1;
    private TransitionSystem system2;
    private static int nextDefaultComponentId = 0;

    protected Query(QueryBuilder builder){
        this.resultList = new ArrayList<>();
        this.queryType = builder.queryType;
        this.system1 = builder.system1;
        this.system2 = builder.system2;
        this.componentName = builder.componentName;
    }

    public abstract void handle();

    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean getResult() {
        return result;
    }

    public void addResultString(String resultString){
        resultList.addAll(Arrays.stream(resultString.split(",")).map(String::trim).collect(Collectors.toList()));
    }

    public QueryType getType() {
        return queryType;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getResultStrings(){
        return String.join("\n", resultList);
    }

    public TransitionSystem getSystem1() {
        return system1;
    }

    public TransitionSystem getSystem2() {
        return system2;
    }

    public enum QueryType {
        REFINEMENT,
        CONSISTENCY,
        IMPLEMENTATION,
        DETERMINISM,
        GET_COMPONENT,
        BISIM_MINIM,
        PRUNE
    }

    public static class QueryBuilder{
        private QueryType queryType;
        private String componentName;
        private TransitionSystem system1;
        private TransitionSystem system2;

        public QueryBuilder queryType(QueryType queryType){
            this.queryType = queryType;
            return this;
        }

        public QueryBuilder componentName(String componentName){
            this.componentName = componentName;
            return this;
        }

        public QueryBuilder system1(TransitionSystem system1){
            this.system1 = system1;
            return this;
        }

        public QueryBuilder system2(TransitionSystem system2){
            this.system2 = system2;
            return this;
        }

        public Query build(){
            if(componentName == null){
                setDefaultComponentName();
            }
            switch (queryType){
                case REFINEMENT:
                    return new RefinementQuery(this);
                case CONSISTENCY:
                    return new ConsistencyQuery(this);
                case PRUNE:
                    return new PruneQuery(this);
                case BISIM_MINIM:
                    return new BisimMinimQuery(this);
                case DETERMINISM:
                    return new DeterminismQuery(this);
                case GET_COMPONENT:
                    return new GetComponentQuery(this);
                case IMPLEMENTATION:
                    return new ImplementationQuery(this);
                default:
                    throw new RuntimeException("Unknown query type");
            }
        }

        private void setDefaultComponentName() {
            componentName = "automaton" + nextDefaultComponentId;
            nextDefaultComponentId++;
        }
    }

}
