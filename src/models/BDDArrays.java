package models;

import lib.CDDLib;

import java.util.ArrayList;
import java.util.List;

public class BDDArrays {
    int traceCount, booleanCount;

    private final List<List<Integer>> variables;
    private final List<List<Integer>> values;

    public BDDArrays(long pointer) {
        traceCount = CDDLib.getNumTracesFromBDDArray(pointer);
        booleanCount = CDDLib.getNumBoolsFromBDDArray(pointer);

        variables = new ArrayList<>();
        values = new ArrayList<>();

        int[] bddValues = CDDLib.getValuesFromBDDArray(pointer);
        int[] bddVariables = CDDLib.getVarsFromBDDArray(pointer);

        for (int i = 0; i < traceCount; i++) {
            List<Integer> valueTrace = new ArrayList<>();
            List<Integer> variableTrace = new ArrayList<>();

            for (int j = 0; j < booleanCount; j++) {
                int index = i * booleanCount + j;
                valueTrace.add(bddValues[index]);
                variableTrace.add(bddVariables[index]);
            }

            values.add(valueTrace);
            variables.add(variableTrace);
        }

        CDDLib.deleteBDDArrays(pointer);
    }

    public List<List<Integer>> getVariables() {
        return variables;
    }

    public List<List<Integer>> getValues() {
        return values;
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("Number of traces: " + traceCount + "\n");
        res.append("Number of bools: " + booleanCount + "\n");

        res.append("Vars: " + "\n");
        for (int i = 0; i < traceCount; i++) {
            res.append("Trace : " + i + "\n");
            for (int j = 0; j < booleanCount; j++) {
                res.append(variables.get(i).get(j));
            }
            res.append("\n");
        }

        res.append("Values: " + "\n");
        for (int i = 0; i < traceCount; i++) {
            res.append("Trace : " + i + "\n");
            for (int j = 0; j < booleanCount; j++) {
                res.append(values.get(i).get(j));
            }
            res.append("\n");
        }
        return res.toString();
    }
}
