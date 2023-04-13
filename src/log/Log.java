package log;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

public class Log {
    private static Urgency urgency = Urgency.All;
    private static final PrintStream printStream = System.out;

    public static void setUrgency(Urgency urgency) {
        Log.urgency = urgency;
    }

    public static void setUrgency() {
        // Makes running tests an opt-in for logging, whereas default is opt-out
        if (isRunningTests()) {
            setUrgency(Urgency.Off);
        } else {
            setUrgency(Urgency.All);
        }
    }

    static {
        setUrgency();
    }

    private static boolean isRunningTests() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    public static void fatal(String message) {
        if (willPrint(Urgency.Fatal)) {
            out(format(message, Urgency.Fatal));
        }
    }

    public static void fatal(String... messages) {
        fatal(String.join(" ", messages));
    }

    public static void fatal(@Nullable Object obj) {
        fatal(
                String.valueOf(obj)
        );
    }

    public static void fatal(Object... objs) {
        fatal(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void fatal(Supplier<T> supplier) {
        if (willPrint(Urgency.Fatal)) {
            fatal(supplier.get());
        }
    }

    public static void fatal() {
        fatal("");
    }

    public static void error(String message) {
        if (willPrint(Urgency.Error)) {
            out(format(message, Urgency.Error));
        }
    }

    public static void error(String... messages) {
        error(String.join(" ", messages));
    }

    public static void error(@Nullable Object obj) {
        error(
                String.valueOf(obj)
        );
    }

    public static void error(Object... objs) {
        error(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void error(Supplier<T> supplier) {
        if (willPrint(Urgency.Error)) {
            error(supplier.get());
        }
    }

    public static void error() {
        error("");
    }

    public static void warn(String message) {
        if (willPrint(Urgency.Warn)) {
            out(format(message, Urgency.Warn));
        }
    }

    public static void warn(String... messages) {
        warn(String.join(" ", messages));
    }

    public static void warn(@Nullable Object obj) {
        warn(
                String.valueOf(obj)
        );
    }

    public static void warn(Object... objs) {
        warn(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void warn(Supplier<T> supplier) {
        if (willPrint(Urgency.Warn)) {
            warn(supplier.get());
        }
    }

    public static void warn() {
        warn("");
    }

    public static void info(String message) {
        if (willPrint(Urgency.Info)) {
            out(format(message, Urgency.Info));
        }
    }

    public static void info(String... messages) {
        info(String.join(" ", messages));
    }

    public static void info(@Nullable Object obj) {
        info(
                String.valueOf(obj)
        );
    }

    public static void info(Object... objs) {
        info(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void info(Supplier<T> supplier) {
        if (willPrint(Urgency.Info)) {
            info(supplier.get());
        }
    }

    public static void info() {
        info("");
    }

    public static void debug(String message) {
        if (willPrint(Urgency.Debug)) {
            out(format(message, Urgency.Debug));
        }
    }

    public static void debug(String... messages) {
        debug(String.join(" ", messages));
    }

    public static void debug(@Nullable Object obj) {
        debug(
                String.valueOf(obj)
        );
    }

    public static void debug(Object... objs) {
        debug(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void debug(Supplier<T> supplier) {
        if (willPrint(Urgency.Debug)) {
            debug(supplier.get());
        }
    }

    public static void debug() {
        debug("");
    }

    public static void trace(String message) {
        if (willPrint(Urgency.Trace)) {
            out(format(message, Urgency.Trace));
        }
    }

    public static void trace(String... messages) {
        trace(String.join(" ", messages));
    }

    public static void trace(@Nullable Object obj) {
        trace(
                String.valueOf(obj)
        );
    }

    public static void trace(Object... objs) {
        trace(
                String.join(
                        " ", Arrays.stream(objs).map(String::valueOf).toArray(String[]::new)
                )
        );
    }

    public static <T> void trace(Supplier<T> supplier) {
        if (willPrint(Urgency.Trace)) {
            trace(supplier.get());
        }
    }

    public static void trace() {
        trace("");
    }

    public static void exception(Exception exception) {
        Log.exception(exception, Urgency.Error);
    }

    public static void exception(Exception exception, Urgency urgency) {
        if (willPrint(urgency)) {
            LogPrintStream logPrintStream = new Log.LogPrintStream(Log.printStream, urgency);
            exception.printStackTrace(logPrintStream);
        }
    }

    public static boolean willPrint(Urgency other) {
        return urgency.level >= other.level;
    }

    private static StackTraceElement getCaller() {
        // We ignore all elements until we are "outside" the Logger
        // Start at "1" As we want to ignore the "Thread.currentThread().getStackTrace()" call
        int index = 1;
        StackTraceElement element = Thread.currentThread().getStackTrace()[index];
        while (Objects.equals(element.getClassName(), Log.class.getName())) {
            element = Thread.currentThread().getStackTrace()[index];
            index++;
        }

        return Thread.currentThread().getStackTrace()[index - 1];
    }

    private static String colorize(String message, Urgency urgency)
        throws IllegalArgumentException {
        switch (urgency) {
            case Fatal: return Ansi.colorize(Ansi.RED_BRIGHT, message);
            case Error: return Ansi.colorize(Ansi.RED, message);
            case Warn: return Ansi.colorize(Ansi.YELLOW, message);
            case Info: return Ansi.colorize(Ansi.GREEN, message);
            case Debug: return Ansi.colorize(Ansi.CYAN, message);
            case Trace: return Ansi.colorize(Ansi.BLACK_BRIGHT, message);
            default:
                throw new IllegalArgumentException("Urgency is not supported");
        }
    }

    private static String format(String message, Urgency urgency) {
        StringBuilder builder = new StringBuilder();

        builder.append("[");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        builder.append(
                Ansi.colorize(Ansi.BLUE_BRIGHT, formatter.format(date))
        );

        builder.append(" ");

        StackTraceElement caller = getCaller();
        builder.append(caller.getFileName());
        builder.append(":");
        builder.append(caller.getLineNumber());

        builder.append(" ");

        builder.append(colorize(urgency.toString(), urgency));

        builder.append("] - ");

        builder.append(message);

        return builder.toString();
    }

    private static void out(String message) {
        printStream.println(message);
    }

    private static class LogPrintStream extends PrintStream {
        private final Urgency urgency;

        public LogPrintStream(OutputStream out, Urgency urgency) {
            super(out);
            this.urgency = urgency;
        }

        @Override
        public void println(Object x) {
            switch (urgency) {
                case Fatal: Log.fatal(x); break;
                case Error: Log.error(x); break;
                case Warn: Log.warn(x); break;
                case Info: Log.info(x); break;
                case Debug: Log.debug(x); break;
                case Trace: Log.trace(x); break;
                default:
                    throw new IllegalArgumentException("Urgency is not supported");
            }
        }
    }

}
