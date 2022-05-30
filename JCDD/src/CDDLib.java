package lib;

import java.io.File;
import java.util.List;

public class CDDLib {

    static {
        List<File> searchPath = List.of(
                new File("lib/" + System.mapLibraryName("JCDD")),
                new File("../lib/" + System.mapLibraryName("JCDD")),
                new File(System.mapLibraryName("JCDD"))
        );
        File lib = null;

        for (var f: searchPath) {
            if (f.exists()) {
                lib = f;
                break;
            }
        }

        if (lib != null) {
            System.load(lib.getAbsolutePath());
        } else {
            System.load(searchPath.get(searchPath.size() - 1).getAbsolutePath()); // Default path
        }
    }

    public static void main(String[] args) {

    }

    public static native int cddInit(int maxSize, int cs, int stackSize);
    public static native void cddDone();
    public static native long allocateCdd();
    public static native void freeCdd(long pointer);
    public static native long conjunction(long lCdd, long rCdd);
    public static native long disjunction(long lCdd, long rCdd);
    public static native long negation(long pointer);
    public static native long reduce(long pointer);
    public static native long interval(int i, int j, int lower, boolean lower_strict, int upper, boolean upper_strict);
    public static native long lower(int i, int j, int lower, boolean strict);
    public static native long upper(int i, int j, int upper, boolean strict);
    public static native int cddNodeCount(long pointer);
    public static native void cddAddClocks(int n);
    public static native long getRootNode(long pointer);
    public static native int getNodeLevel(long pointer);
    public static native boolean isElemArrayNullTerminator(long cddNode_pointer, int index);
    public static native long getChildFromElemArray(long cddNode_pointer, int index);
    public static native int getBoundFromElemArray(long cddNode_pointer, int index);
    public static native long cddFromDbm(int[] dbm, int dim);
    public static native void cddPrintDot(long cddPointer);
    public static native void cddPrintDot(long cddPointer, String filePath);
    public static native int addBddvar(int amount);
    public static native long cddBddvar(int level);
    public static native long cddNBddvar(int level);
    public static native boolean isTrue(long cddNodePointer);
    public static native boolean isFalse(long cddNodePointer);
    public static native long cddTrue();
    public static native long cddFalse();
    public static native boolean isTerminal(long cddPointer);
    public static native long delay(long cddPointer);
    public static native long delayInvar(long cddPointer, long cddInvarPointer);
    public static native long exist(long cddPointer, int[] levels, int[] clocks);
    public static native long past(long cddPointer);
    public static native boolean isBDD(long cddPointer);
    public static native long removeNegative(long cddPointer);
    public static native long applyReset(long cddPointer, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues);
    public static native long minus(long lCdd, long rCdd);
    public static native long copy(long cddPointer);
    public static native long transition(long cddPointer, long guardCddPointer, int[] clockResets, int[] clockValues, int[] boolResets, int[] boolValues);
    public static native long transitionBack(long cddPointer, long guardCddPointer, long updateCddPointer, int[] clockResets, int[] boolResets);
    public static native long transitionBackPast(long cddPointer, long guardCddPointer, long updateCddPointer, int[] clockResets, int[] boolResets);
    public static native long predt(long targetCddPointer, long safeCddPointer);
    public static native long extractBddAndDbm(long cddPointer);
    public static native long getCddPartFromExtractionResult(long extractionResultPointer);
    public static native long getBddPartFromExtractionResult(long extractionResultPointer);

    public static native long bddToArray(long cddPointer, int numBools);
    public static native int[] getVarsFromBDDArray(long bddArrayPointer);
    public static native int[] getValuesFromBDDArray(long bddArrayPointer);
    public static native int getNumTracesFromBDDArray(long bddArrayPointer);
    public static native int getNumBoolsFromBDDArray(long bddArrayPointer);
    public static native boolean deleteBDDArrays(long bddArrayPointer);
    public static native boolean deleteCDDExtractionResult(long extractionResultPointer);


    public static native int[] dbm_extrapolateMaxBoundsNoClose(int[] dbm, int dim, int[] max);

    public static native int[] getDbmFromExtractionResult(long extractionResultPointer);
    public static native boolean cddEquiv(long lCdd, long rCdd);
}
