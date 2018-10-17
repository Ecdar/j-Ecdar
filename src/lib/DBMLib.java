package lib;

public class DBMLib {

    // here you must define every method you want to use from the DBM library
    public static native int boundbool2raw(int bound, boolean isStrict);
    public static native int raw2bound(int raw);
}
