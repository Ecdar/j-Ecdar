package logic;

import Exceptions.InvalidQueryException;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private String queryString;
    private QueryType queryType;
    private List<String> resultList;
    private boolean result;
    private String componentName;

    public Query(String query) throws Exception {
        queryString = query.replaceAll("\\s+", "");
        isQueryValid(queryString);
        resultList = new ArrayList<>();
        if(queryString.contains("save-as")){
            String[] saveQuery = queryString.split("save-as");
            queryString = saveQuery[0];
            componentName = saveQuery[1];
        }
        determineQueryType();
    }

    private void determineQueryType(){
        if(queryString.contains("refinement")){
            queryType = QueryType.REFINEMENT;
            queryString = queryString.replace("refinement:", "");
        } else if(queryString.contains("consistency")){
            queryType = QueryType.CONSISTENCY;
            queryString = queryString.replace("consistency:", "");
        } else if(queryString.contains("implementation")){
            queryType = QueryType.IMPLEMENTATION;
            queryString = queryString.replace("implementation:", "");
        } else if(queryString.contains("determinism")){
            queryType = QueryType.DETERMINISM;
            queryString = queryString.replace("determinism:", "");
        } else if(queryString.contains("get-component")){
            queryType = QueryType.GET_COMPONENT;
            queryString = queryString.replace("get-component:", "");
        } else if(queryString.contains("bisim-minim")){
            queryType = QueryType.BISIM_MINIM;
            queryString = queryString.replace("bisim-minim:", "");
        } else if(queryString.contains("prune")){
            queryType = QueryType.PRUNE;
            queryString = queryString.replace("prune:", "");
        }
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

    public String getQuery() {
        return queryString;
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

    public enum QueryType {
        REFINEMENT,
        CONSISTENCY,
        IMPLEMENTATION,
        DETERMINISM,
        GET_COMPONENT,
        BISIM_MINIM,
        PRUNE
    }

    public static void isQueryValid(String query) throws Exception {
        checkRefinementSyntax(query);
        isParBalanced(query);
        beforeAfterParantheses(query);
        checkSyntax(query);
    }

    private static void checkRefinementSyntax(String query) throws InvalidQueryException {
        if (query.contains("<=") && !query.contains("refinement:")) throw new InvalidQueryException("Expected: \"refinement:\"");

        if (query.matches(".*<=.*<=.*")) throw new InvalidQueryException("There can only be one refinement");
    }

    private static void isParBalanced(String query) throws InvalidQueryException {
        int counter = 0;

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                counter++;
            }
            if (query.charAt(i) == ')') {
                counter--;
            }
        }

        if (counter != 0) throw new InvalidQueryException("Parentheses are not balanced");
    }

    private static void beforeAfterParantheses(String query) throws InvalidQueryException {
        String testString = "/=|&:(";

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '(') {
                if (i != 0) {
                    if (testString.indexOf(query.charAt(i - 1)) == -1)
                        throw new InvalidQueryException("Before opening Parentheses can be either operator or second Parentheses");
                }
                if (i + 1 < query.length()) {
                    if (!(query.charAt(i + 1) == '(' || Character.isLetter(query.charAt(i + 1)) || Character.isDigit(query.charAt(i + 1))))
                        throw new InvalidQueryException("After opening Parentheses can be either other Parentheses or component");
                }
            }
        }
    }

    private static void checkSyntax(String query) throws InvalidQueryException {
        String testString = "/=|&:";
        for (int i = 0; i < query.length(); i++) {
            if (testString.indexOf(query.charAt(i)) != -1) {
                return;
            }
        }
        throw new InvalidQueryException("Incorrect syntax, does not contain any feature");
    }
}
