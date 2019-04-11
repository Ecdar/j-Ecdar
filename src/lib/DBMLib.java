package lib;

public class DBMLib {

    // here you must define every method you want to use from the DBM library
    public static native int boundbool2raw(int bound, boolean isStrict);

    public static native int raw2bound(int raw);

    public static native int[] dbm_init(int[] dbm, int dim);

    public static native int[] dbm_zero(int[] dbm, int dim);

    public static native int[] dbm_constrain1(int[] dbm, int dim, int i, int j, int constraint, boolean strict);

    public static native int[] dbm_up(int[] dbm, int dim);

    public static native boolean dbm_isSubsetEq(int[] dbm1, int[] dbm2, int dim);

    public static native int[] dbm_updateValue(int[] dbm, int dim, int clockIndex, int value);

    public static native boolean dbm_isValid(int[] dbm, int dim);

    public static native boolean dbm_intersection(int[] dbm1, int[] dbm2, int dim);

    public static native int[] dbm_freeAllDown(int[] dbm, int dim);

    public static native int[] dbm_freeDown(int[] dbm, int dim, int clockIndex);

    public static native boolean dbm_rawIsStrict(int raw);

    public static native int dbm_addRawRaw(int raw1, int raw2);

    public static native int[][] dbm_minus_dbm(int[] dbm1, int[] dbm2, int dim);

    public static native int[][] fed_minus_dbm(int[][]fed, int[] dbm, int dim);

    public static native int[][] fed_minus_fed(int[][]fed1, int[][] fed2, int dim);
}
