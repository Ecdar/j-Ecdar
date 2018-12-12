package lib;

public class DBMLib {

    // here you must define every method you want to use from the DBM library
    public static native int boundbool2raw(int bound, boolean isStrict);

    public static native int raw2bound(int raw);

    public static native int[] dbm_init(int[] dbm, int dim);

    public static native int[] dbm_zero(int[] dbm, int dim);

    public static native int[] dbm_constrain1(int[] dbm, int dim, int i, int j, int constraint);

    public static native int[] dbm_up(int[] dbm, int dim);

    public static native boolean dbm_isSubsetEq(int[] dbm1, int[] dbm2, int dim);

    public static native int[] dbm_updateValue(int[] dbm, int dim, int x, int value);

    public static native boolean dbm_isValid(int[] dbm, int dim);
}
