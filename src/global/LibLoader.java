package global;

import java.io.File;

public class LibLoader {

    public static void load() {
        String fileName = "src/" + System.mapLibraryName("DBM");
        File lib = new File(fileName);
        System.load(lib.getAbsolutePath());
    }
}
