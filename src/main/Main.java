package main;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }
}