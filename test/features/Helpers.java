package features;

import lib.DBMLib;
import log.Log;

public class Helpers {



    // Method to nicely print DBM for testing purposes.
    // The boolean flag determines if values of the zone will be converted from DBM format to actual bound of constraint
    public static void printDBM(int[] x, boolean toConvert, boolean showStrictness) {
        int length = x.length;
        int dim = (int) Math.sqrt(length);
        int intLength = 0;
        int toPrint = 0;

        Log.trace("---------------------------------------");
        for (int i = 0, j = 1; i < length; i++, j++) {

            toPrint = toConvert ? DBMLib.raw2bound(x[i]) : x[i];

            System.out.print(toPrint);

            if (showStrictness) {
                String strictness = DBMLib.dbm_rawIsStrict(x[i]) ? " < " : " <=";
                System.out.print(strictness);
            }
            if (j == dim) {
                Log.trace();
                if (i == length - 1) Log.trace("---------------------------------------");
                j = 0;
            } else {
                intLength = String.valueOf(toPrint).length();
                for (int k = 0; k < 14 - intLength; k++) {
                    System.out.print(" ");
                }
            }
        }
    }
}
