package lib;

public class DBMLib {

    // here you must define every method you want to use from the DBM library
    public static native int boundbool2raw(int bound, boolean isStrict);
    public static native int raw2bound(int raw);
    public static native Constraint constraint(int i, int j, int bound, boolean isStrict);
    public static native int[] dbm_init(int[] dbm, int dim);
    public static native int[] dbm_zero(int[] dbm, int dim);
}
