package models;

import lib.CDDLib;

public class CddExtractionResult {

    private long pointer;
    private CDD cddPart;
    private CDD bddPart;
    private int[] dbm;

    public CddExtractionResult(long pointer) {
        this.pointer = pointer;
        cddPart = importCddPart();
        bddPart = importBddPart();
        dbm = importDbm();

    }




    public CDD getCddPart() {
        return cddPart;
    }

    public CDD getBddPart() {
        return bddPart;
    }

    public int[] getDbm() {
        return dbm;

    }



    public CDD importCddPart() {
        checkForNull();

        if(cddPart != null){
            return cddPart;
        }else{
            return new CDD(CDDLib.getCddPartFromExtractionResult(pointer));
        }
    }

    public CDD importBddPart() {
        checkForNull();

        if(bddPart != null){
            return bddPart;
        }else{
            return new CDD(CDDLib.getBddPartFromExtractionResult(pointer));
        }
    }

    public int[] importDbm() {
        checkForNull();

        if(dbm != null){
            return dbm;
        }else{
            return CDDLib.getDbmFromExtractionResult(pointer);
        }
    }

    private void checkForNull(){
        if(pointer == 0){
            throw new NullPointerException("CDD extraction result is null");
        }
    }
}
