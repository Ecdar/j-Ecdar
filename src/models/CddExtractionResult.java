package models;

import lib.CDDLib;

import java.util.Objects;

public class CddExtractionResult {
    private final long pointer;
    private final CDD cddPart;
    private final CDD bddPart;
    private final int[] dbm;

    public CddExtractionResult(long pointer) {
        this.pointer = pointer;
        cddPart = importCddPart();
        bddPart = importBddPart();
        dbm = importDbm();
        CDDLib.deleteCDDExtractionResult(this.pointer);
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

    public CDD importCddPart()
            throws NullPointerException {
        checkForNull();
        return Objects.requireNonNullElseGet(
                cddPart, () -> new CDD(CDDLib.getCddPartFromExtractionResult(pointer))
        );
    }

    public CDD importBddPart()
            throws NullPointerException {
        checkForNull();
        return Objects.requireNonNullElseGet(
                bddPart, () -> new CDD(CDDLib.getBddPartFromExtractionResult(pointer))
        );
    }

    public int[] importDbm()
            throws NullPointerException {
        checkForNull();
        return Objects.requireNonNullElseGet(
                dbm, () -> CDDLib.getDbmFromExtractionResult(pointer)
        );
    }

    private void checkForNull() {
        if (pointer == 0) {
            throw new NullPointerException("CDD extraction result is null");
        }
    }
}
