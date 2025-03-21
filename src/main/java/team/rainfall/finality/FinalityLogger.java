package team.rainfall.finality;

import team.rainfall.finality.loader.util.Localization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * <p>The FinalityLogger class provides logging functionality for the application.</p>
 * <p>It supports different log levels such as info, debug, warning, error, and important messages.</p>
 * <p>It also writes logs to a file and formats the output with colors for better readability.</p>
 *
 * @author RedreamR
 */
public class FinalityLogger {

    public static AlternativeOutputStream alternativeOutputStream = new AlternativeOutputStream(System.out);
    public static final int STACKTRACE_LIMIT = 10;
    public static OutputStreamWriter logStream = null;
    public static boolean isDebug = false;
    public static final String RED_COLOR = "\033[31m";
    public static final String RED_BACKGROUND = "\033[41m";
    public static final String WHITE_BACKGROUND = "\033[47m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String GRAY_BACKGROUND = "\033[100m";
    public static final String BLACK_COLOR = "\033[30m";
    public static final String PURPLE_BACKGROUND = "\033[45m";
    public static final String RESET = "\033[0m";

    /**
     * <p>Initializes the logger by creating or resetting the log file and setting up the output streams.</p>
     *
     * @author RedreamR
     */
    public static void init() {
        try {
            File file = new File("./loader.log");
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream("./loader.log");
            fos.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            fos.flush();
            logStream = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            //输出系统的日期
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            logStream.write("Logger initiated at "+ dateTime.format(formatter) + "\n");
            logStream.flush();
            System.setErr(alternativeOutputStream);
            System.setOut(alternativeOutputStream);
        } catch (IOException ignored) {

        }
    }

    /**
     * <p>Logs an important message.</p>
     *
     * @param message the message to log
     * @author RedreamR
     */
    public static void important(String message){
        alternativeOutputStream.bypassing = true;
        System.out.println(PURPLE_BACKGROUND + "[Important] " + message + RESET);
        output("[Important] " + message);
        alternativeOutputStream.bypassing = false;
    }

    /**
     * <p>Logs a localized info message.</p>
     *
     * @param message the message key to localize and log
     * @author RedreamR
     */
    public static void localizeInfo(String message){
        info(String.format(Localization.bundle.getString(message)));
    }

    public static void info(String message) {
        alternativeOutputStream.bypassing = true;
        System.out.println(WHITE_BACKGROUND + BLACK_COLOR + "[Info] " + message + RESET);
        output("[Info] " + message);
        alternativeOutputStream.bypassing = false;
    }

    /**
     * <p>Logs an info message.</p>
     *
     * @param message the message to log
     * @author RedreamR
     */
    public static void error(String message) {
        alternativeOutputStream.bypassing = true;
        System.err.println(RED_BACKGROUND + "[Error] " + message + RESET);
        output("[Error] " + message);
        alternativeOutputStream.bypassing = false;
    }

    /**
     * <p>Logs an error message with a throwable stack trace.</p>
     *
     * @param message   the message to log
     * @param throwable the throwable to log
     * @author RedreamR
     */
    public static void error(String message, Throwable throwable) {
        alternativeOutputStream.bypassing = true;
        System.err.println(RED_BACKGROUND+"[Error] " + message + RESET);
        System.err.println(RED_COLOR+getStackTraceAsString(throwable,true)+RESET);
        output("[Error] " + message);
        output(getStackTraceAsString(throwable,false));
        alternativeOutputStream.bypassing = false;
    }


    /**
     * <p>Converts a throwable stack trace to a string.</p>
     *
     * @param throwable      the throwable to convert
     * @param stacktraceLimit whether to limit the stack trace length
     * @return the stack trace as a string
     * @author Greyeonm, RedreamR
     */
    private static String getStackTraceAsString(Throwable throwable,boolean stacktraceLimit) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString());
        if(throwable.getMessage() != null){
            sb.append(":").append(throwable.getMessage());
        }
        int i = 1;
        for (StackTraceElement element : throwable.getStackTrace()) {
            if(i >= STACKTRACE_LIMIT && stacktraceLimit){
                sb.append("\n").append("... ").append(throwable.getStackTrace().length - i).append(" more");
                break;
            }
            i++;
            sb.append("\n").append("at ").append(element.toString());
        }
        if(throwable.getCause() != null) {
            sb.append("\nCaused by: ").append(getStackTraceAsString(throwable.getCause(),stacktraceLimit));
        }

        return sb.toString();
    }

    /**
     * <p>Logs a debug message if debug mode is enabled.</p>
     *
     * @param message the message to log
     * @author RedreamR
     */
    public static void debug(String message) {
        alternativeOutputStream.bypassing = true;
        if (isDebug) {
            System.out.println(GRAY_BACKGROUND + "[Debug] " + message + RESET);
            output("[Debug] " + message);
        }
        alternativeOutputStream.bypassing = false;
    }

    /**
     * <p>Logs a warning message.</p>
     *
     * @param message the message to log
     * @author RedreamR
     */
    public static void warn(String message) {
        alternativeOutputStream.bypassing = true;
        System.out.println(YELLOW_BACKGROUND + BLACK_COLOR + "[Warning] " + message + RESET);
        output("[Warning] "+message);
        alternativeOutputStream.bypassing = false;
    }


    /**
     * <p>Writes a message to the log file.</p>
     *
     * @param message the message to write
     * @author RedreamR
     */
    public static void output(String message){
        try{
            logStream.write(message+'\n');
            logStream.flush();
        }catch (IOException ignored){

        }
    }

}
