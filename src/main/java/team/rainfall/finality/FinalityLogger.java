package team.rainfall.finality;

public class FinalityLogger {
    public static final boolean isDebug = true;
    public static void info(String message) {
        System.out.println("[I] "+message);
    }
    public static void error(String message) {
        System.err.println("[E] "+message);
    }
    public static void error(String message,Throwable throwable){
        System.err.println("[E] "+message+"\n"+throwable.getMessage());
        if(isDebug){
            throwable.printStackTrace();
        }
    }
    public static void debug(String message) {
        if(isDebug) {
            System.out.println("[D] " + message);
        }
    }
}
