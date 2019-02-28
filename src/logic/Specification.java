package logic;

import lib.DBMLib;
import models.*;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Specification {
    private Deque<State> waiting;
    private List<State> passed;
    private TransitionSystem ts;

    public Specification(TransitionSystem ts) {
        this.ts = ts;
        this.waiting = new ArrayDeque<>();
        this.passed = new ArrayList<>();
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }

    public boolean isSpecification() {
        waiting.push(ts.getInitialState());
        List<Channel> channels = new ArrayList<>(ts.getInputs());
        channels.addAll(ts.getOutputs());
        channels.addAll(ts.getSyncs());
        while (!waiting.isEmpty()) {
            State currentState = waiting.pop();
            currentState.getZoneValues();
            boolean outputEdge = false;
            boolean invariant = false;
            if (currentState.getInvariants().size() > 0) {
                invariant = true;
            }
            for (Channel action : channels) {

                List<Transition> transitions = ts.getNextTransitions(currentState, action);

                if (transitions.size() >= 2) {
                    if (!checkDeterminism(transitions)) {
                        return false;
                    }
                }
                if (invariant) {
                    if (ts.getOutputs().contains(action)) {
                        for (Transition transition : transitions) {
                            outputEdge = true;
                        }
                    }
                }


            }
            if (!outputEdge && invariant)
                return false;
            passed.add(currentState);
        }

        return true;
    }

    private boolean checkDeterminism(List<Transition> transitions) {
        for (int i = 0; i < transitions.size(); i++) {
            waiting.add(transitions.get(i).getTarget());
            for (int j = i+1; j < transitions.size(); j++) {

                transitions.get(i).getTarget().applyGuards(transitions.get(i).getGuards(), ts.getClocks());
                transitions.get(j).getTarget().applyGuards(transitions.get(j).getGuards(), ts.getClocks());
                printDBM(transitions.get(i).getTarget().getZone(),true);
                printDBM(transitions.get(j).getTarget().getZone(),true);
                boolean check = DBMLib.dbm_intersection(transitions.get(i).getTarget().getZone(),
                        transitions.get(j).getTarget().getZone(), ts.getDbmSize());
                if (check)
                    return false;
            }
        }
        return true;
    }

    public static void printDBM(int[] x, boolean toConvert){
        int length = x.length;
        int dim = (int) Math.sqrt(length);
        int intLength = 0;
        int toPrint = 0;

        System.out.println("---------------------------------------");
        for(int i = 0,j = 1; i < length; i++, j++){

            if(toConvert) toPrint = DBMLib.raw2bound(x[i]);
            else toPrint = x[i];

            System.out.print(toPrint);
            if(j == dim){
                System.out.println();
                if(i == length - 1) System.out.println("---------------------------------------");
                j = 0;
            }
            else{
                intLength = String.valueOf(toPrint).length();
                for (int k = 0; k < 14 - intLength; k++)
                {
                    System.out.print(" ");
                }
            }
        }
    }









}
