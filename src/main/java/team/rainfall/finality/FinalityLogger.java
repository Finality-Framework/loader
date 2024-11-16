package team.rainfall.finality;

public class FinalityLogger {
    public static final boolean isDebug = true;
    public static final String RED_BACKGROUND = "\033[41m";
    public static final String WHITE_BACKGROUND = "\033[47m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String GRAY_BACKGROUND = "\033[100m";
    public static final String BLACK_COLOR = "\033[30m";
    public static final String RESET = "\033[0m";
    public static void info(String message) {
        System.out.println(WHITE_BACKGROUND+BLACK_COLOR+"[Info] "+message+RESET);
    }
    public static void error(String message) {
        System.err.println(RED_BACKGROUND+"[Error] "+message+RESET);
    }
    public static void error(String message,Throwable throwable){
        System.err.println("[Error] "+message+"\n"+throwable.getMessage());
        if(isDebug){
            throwable.printStackTrace();
        }
    }
    public static void debug(String message) {
        if(isDebug) {
            System.out.println(GRAY_BACKGROUND+"[Debug] " + message+RESET);
        }
    }
    public static void warn(String message) {
        System.out.println(YELLOW_BACKGROUND+BLACK_COLOR+"[Warning] "+message+RESET);
    }
}
