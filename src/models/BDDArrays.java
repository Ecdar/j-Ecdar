package models;

import lib.CDDLib;

public class BDDArrays {

    private long pointer;
    private int[][] vars;
    private int[][] values;

    public BDDArrays(long pointer) {
        this.pointer = pointer;
        vars = importVars();
        values = importValues();
    }

    public int[][] getVars() {
        return vars;
    }

    public int[][] getValues() {
        return values;
    }

    public int[][] importVars() {
        checkForNull();
        if(vars != null){
            return vars;
        }else{
            return(CDDLib.getVarsFromBDDArray(pointer));
        }
    }

    public int[][] importValues() {
        checkForNull();

        if(values != null){
            return values;
        }else{
            return (CDDLib.getValuesFromBDDArray(pointer));
        }
    }

    private void checkForNull(){
        if(pointer == 0){
            throw new NullPointerException("BDD arrays result is null");
        }
    }
}
