import java.io.File;

public class DBMLib {

  // here you must define every method you want to use from the DBM library
  public native int boundbool2raw(int bound, boolean isStrict);
  public native int raw2bound(int raw);

  public static void main(String[] args) {

      File lib = new File(System.mapLibraryName("DBM"));
      System.load(lib.getAbsolutePath());

      DBMLib dbm = new DBMLib();
      int res1 = dbm.boundbool2raw(5, true);
      int res2 = dbm.boundbool2raw(5, false);
      int res3 = dbm.raw2bound(10);

      System.out.println("boundbool2raw: " + res1);
      System.out.println("boundbool2raw: " + res2);
      System.out.println("raw2bound: " + res3);

  }
}
