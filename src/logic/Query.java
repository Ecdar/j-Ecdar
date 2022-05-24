package logic;

import Exceptions.InvalidQueryException;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private QueryType queryType;
    private List<String> resultList;
    private boolean result;
    private String componentName;
    private String system1;
    private String system2;
    private static int nextDefaultComponentId = 0;

    private Query(QueryBuilder builder){
        this.resultList = new ArrayList<>();
        this.queryType = builder.queryType;
        this.system1 = builder.system1;
        this.system2 = builder.system2;
        this.componentName = builder.componentName;
    }


    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean getResult() {
        return result;
    }

    public void addResultString(String resultString){
        resultList.add(resultString);
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

    public String getSystem1() {
        return system1;
    }

    public String getSystem2() {
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
        private String system1;
        private String system2;

        public QueryBuilder queryType(QueryType queryType){
            this.queryType = queryType;
            return this;
        }

        public QueryBuilder componentName(String componentName){
            this.componentName = componentName;
            return this;
        }

        public QueryBuilder system1(String system1){
            this.system1 = system1;
            return this;
        }

        public QueryBuilder system2(String system2){
            this.system2 = system2;
            return this;
        }

        public Query build(){
            if(componentName == null){
                setDefaultComponentName();
            }
            return new Query(this);
        }

        private void setDefaultComponentName() {
            componentName = "automaton" + nextDefaultComponentId;
            nextDefaultComponentId++;
        }
    }

}
