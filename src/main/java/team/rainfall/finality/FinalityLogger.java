package team.rainfall.finality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

public class FinalityLogger {
    public static final int STACKTRACE_LIMIT = 10;
    public static OutputStreamWriter logStream = null;
    public static boolean isDebug = false;
    public static final String RED_COLOR = "\033[31m";
    public static final String RED_BACKGROUND = "\033[41m";
    public static final String WHITE_BACKGROUND = "\033[47m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String GRAY_BACKGROUND = "\033[100m";
    public static final String BLACK_COLOR = "\033[30m";
    public static final String RESET = "\033[0m";

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
        } catch (IOException ignored) {

        }
    }

    public static void info(String message) {
        System.out.println(WHITE_BACKGROUND + BLACK_COLOR + "[Info] " + message + RESET);
        output("[Info] " + message);
    }

    public static void error(String message) {
        System.err.println(RED_BACKGROUND + "[Error] " + message + RESET);
        output("[Error] " + message);
    }

    public static void error(String message, Throwable throwable) {
        System.err.println(RED_BACKGROUND+"[Error] " + message + RESET);
        if (isDebug) {
            System.err.println(RED_COLOR+getStackTraceAsString(throwable,true)+RESET);
        }
        output("[Error] " + message);
        if (isDebug) {
            output(getStackTraceAsString(throwable,false));
        }
    }

    //将异常堆栈转换为字符串
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
        return sb.toString();
    }


    public static void debug(String message) {
        if (isDebug) {
            System.out.println(GRAY_BACKGROUND + "[Debug] " + message + RESET);
            output("[Debug] " + message);
        }

    }

    public static void warn(String message) {
        System.out.println(YELLOW_BACKGROUND + BLACK_COLOR + "[Warning] " + message + RESET);
        output("[Warning] "+message);
    }
    public static void output(String message){
        try{
            logStream.write(message+'\n');
            logStream.flush();
        }catch (IOException ignored){

        }
    }

}
