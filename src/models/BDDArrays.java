package models;

import io.grpc.internal.DnsNameResolver;
import lib.CDDLib;

import java.util.ArrayList;
import java.util.List;

public class BDDArrays {

    private long pointer;
    int numTraces;
    int numBools;
    private List<List<Integer>> vars;
    private List<List<Integer>> values;

    public BDDArrays(long pointer) {
        this.pointer = pointer;
        numTraces= importNumTraces();
        numBools = importNumBools();
        vars = importVars();
        values = importValues();
    }

    public String toString()
    {
        StringBuffer res = new StringBuffer();
        res.append("Number of traces: " + numTraces + "\n");
        res.append("Number of bools: " + numBools + "\n");

        res.append("Vars: " + "\n");
        for (int i=0; i<numTraces; i++) {
            res.append("Trace : " + i + "\n");
            for (int j=0; j<numBools; j++)
            {
                res.append(vars.get(i).get(j));
            }
            res.append("\n");
        }

        res.append("Values: " + "\n");
        for (int i=0; i<numTraces; i++) {
            res.append("Trace : " + i + "\n");
            for (int j=0; j<numBools; j++)
            {
                res.append(values.get(i).get(j));
            }
            res.append("\n");
        }
        return res.toString();
    }

    public List<List<Integer>>  getVars() {
        return vars;
    }

    public List<List<Integer>>  getValues() {
        return values;
    }

    public int importNumTraces() {
           return CDDLib.getNumTracesFromBDDArray(pointer);
    }

    public int importNumBools() {
        return CDDLib.getNumBoolsFromBDDArray(pointer);
    }

    public List<List<Integer>> importVars() {
        checkForNull();
        if(vars != null){
            return vars;
        }else{
            List<List<Integer>> result = new ArrayList<>();
            int[] vars=CDDLib.getVarsFromBDDArray(pointer);
            for (int i=0; i<numTraces; i++) {
                List<Integer> trace = new ArrayList<>();
                for (int j = 0; j < numBools; j++) {
                    trace.add(vars[i * numBools + j]);
                }
                result.add(trace);
            }
            return result;
        }
    }

    public List<List<Integer>> importValues() {
        checkForNull();
        if(values != null){
            return values;
        }else{
            List<List<Integer>> result = new ArrayList<>();
            int[] vals=CDDLib.getValuesFromBDDArray(pointer);
            for (int i=0; i<numTraces; i++) {
                List<Integer> trace = new ArrayList<>();
                for (int j = 0; j < numBools; j++) {
                    trace.add(vals[i * numBools + j]);
                }
                result.add(trace);
            }
            return result;
        }
    }

    private void checkForNull(){
        if(pointer == 0){
            throw new NullPointerException("BDD arrays result is null");
        }
    }
}
