package features;

import lib.DBMLib;

class Helpers {

    // Method to nicely print DBM for testing purposes.
    // The boolean flag determines if values of the zone will be converted from DBM format to actual bound of constraint
    public static void printDBM(int[] x, boolean toConvert){
        int length = x.length;
        int dim = (int) Math.sqrt(length);
        int intLength = 0;
        int toPrint = 0;

        System.out.println("---------------------------------------");
        for(int i = 0,j = 1; i < length; i++, j++){

            if(toConvert) toPrint = DBMLib.raw2bound(x[i]);
            else toPrint = x[i];

            System.out.print(toPrint);
            if(j == dim){
                System.out.println();
                if(i == length - 1) System.out.println("---------------------------------------");
                j = 0;
            }
            else{
                intLength = String.valueOf(toPrint).length();
                for (int k = 0; k < 14 - intLength; k++)
                {
                    System.out.print(" ");
                }
            }
        }
    }
}
