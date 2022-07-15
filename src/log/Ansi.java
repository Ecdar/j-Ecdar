package log;

public class Ansi {
    private static final String ESC = "\033";
    private static final String PREFIX = ESC + "[";
    private static final String POSTFIX = "m";
    private static final String RESET = PREFIX + "0" + POSTFIX;

    public static final String RED = "0;31m";
    public static final String GREEN = "0;32m";
    public static final String YELLOW = "0;33m";
    public static final String CYAN = "0;36m";
    public static final String BLACK_BRIGHT = "0;90m";
    public static final String RED_BRIGHT = "0;91m";
    public static final String BLUE_BRIGHT = "0;94m";

    public static String colorize(String color, String message) {
        return PREFIX + color + message + RESET;
    }
}
