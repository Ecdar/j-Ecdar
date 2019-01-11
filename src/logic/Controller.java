package logic;

import models.Component;
import parser.Parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private static String folderLoc;
    private static List<String> Queries = new ArrayList<>();
    private static ArrayList<Component> cmpt = new ArrayList<>();
    private static final int REFINEMENT = 0;
    private static final int COMPOSITION = 1;
    private static final int CONJUNCTION = 2;
    private static final int QUOTIENT = 3;
    private int k= 0;
    public Controller(){

    }
    public List<Boolean> parseFiles(String locQuery){
        folderLoc = "";
        cmpt.clear();
        Queries.clear();
        separateLocQuery(locQuery); // Separates location and Queries
        parseComponents(folderLoc); // Parses components and adds them to local variable cmpt
        return runQueries();
    }
    public void separateLocQuery(String locQuery){
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll( Arrays.asList(locQuery.split(",")));
        folderLoc = temp.get(0);
        temp.remove(0);
        Queries.addAll(temp);
    }
    public ArrayList<Component> parseComponents(String folderLocation){
        cmpt=Parser.parse(folderLocation);
        return cmpt;
    }
    public List<Boolean> runQueries(){
        List<Boolean> returnlist = new ArrayList<Boolean>();
        for (int i = 0; i < Queries.size(); i++){
            Queries.set(i,Queries.get(i).replaceAll("\\s+",""));
            if (Queries.get(i).contains("refinement")){
                List<String> refSplit = Arrays.asList(Queries.get(i).replace("refinement:","").split("<="));
                Refinement ref = new Refinement(runQuery(refSplit.get(0)), runQuery(refSplit.get(1)));
                returnlist.add(ref.check());
            }
            //add if contains specification or smth else
        }
        return returnlist;
    }
    public TransitionSystem runQuery(String part){
        ArrayList<TransitionSystem> transitionSystems = new ArrayList<>();
        if (part.charAt(0) == '(' && part.length() >0) {
            part = part.substring(1);
        }
        int feature = -1;
        outerLoop:
        for (int i = 0; i < part.length();i++) {
            if (part.charAt(i) == '(') {
                int tempPosition = checkParentheses(part);
                transitionSystems.add(runQuery(part.substring(i, tempPosition)));
                part = part.substring(tempPosition, part.length());
                i = 0;

            }

            if(Character.isLetter(part.charAt(i)) || Character.isDigit(part.charAt(i))){

                int j = 0;
                boolean check=true;
                while (check){
                    if(i+j < part.length()) {
                        if (!Character.isLetter(part.charAt(i + j)) && !Character.isDigit(part.charAt(i + j))) {
                            transitionSystems.add(new SimpleTransitionSystem(checkComponent(part.substring(i, j + i))));
                            k++;
j--;
                            check = false;
                        }
                    } else {
                        transitionSystems.add(new SimpleTransitionSystem(checkComponent(part.substring(i, j + i))));
                        break outerLoop;}
                    j++;
                }
                i = i +j;
            }
            //if(i < part.length()) {return transitionSystems.get(0);}

                switch (part.charAt(i)) {
                    case '|':  feature = COMPOSITION ;
                        break;
                    case '&': feature = CONJUNCTION ;
                        break;
                    case '/': feature = QUOTIENT;
                        break;
                    default: ;
                        break;
                }

        }
        switch (feature) {
            case COMPOSITION:  return new Composition(transitionSystems);
            case CONJUNCTION: return new Conjunction(transitionSystems);
            case QUOTIENT: ;
                break;
            default: ;
                break;
        }

        return transitionSystems.get(0);
    }
    private Component checkComponent(String str){
        for (int i = 0; i<cmpt.size();i++){
            if (cmpt.get(i).getName().equalsIgnoreCase(str)){
                return cmpt.get(i);

            }
        }
        System.out.println("Component does not exist  "+str);
        return null;
    }

    private int checkParentheses(String smth){
        int balanced = 0;
        boolean seePara = false;
        for (int i = 0; i< smth.length();i++){
            if(smth.charAt(i) == '('){
                balanced++;
                seePara = true;
            }
            else if(smth.charAt(i) == ')' && seePara){
                balanced--;
            }
            if(balanced == 0 && seePara){
                return i;
            }
        }
        return -1;
    }


}
