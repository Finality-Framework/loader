package team.rainfall.luminosity;

public class test {
    public static Object main(String[] args) {
        if(args[0].equals("test")){
            return "1";
        }
        if(args[0].equals("test2")){
            return 2;
        }
        if(args[0].equals("test3")){
            return new Object();
        }
        return null;
    }
}
