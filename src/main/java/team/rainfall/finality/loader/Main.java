//Lesson learnt: Don't play around with IDEA's ‘security’ remove!
//Otherwise, you'll have to use a decompiler to retrieve the code, like I did!
//RedreamR 24.11.8
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.*;
import java.util.*;

import com.formdev.flatlaf.FlatIntelliJLaf;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;


//Entry point of Loader,but it does nothing.

public class Main {
    public static final String VERSION = "1.2.3";
    public static final String STEAM_MANAGER_CLASS = "aoh.kingdoms.history.mainGame.Steam.SteamManager";
    public static String LAUNCHER_CLASS = "aoh.kingdoms.history.mainGame.desktop.DesktopLauncher";
    public static ArrayList<String> localMods = new ArrayList<>();
    public static void main(String[] args) {
        try {
            FlatIntelliJLaf.install();
            Loader.loaderMain(args);
        }catch (Throwable e){
            FinalityLogger.error("Unknown err",e);
            ErrorCode.showInternalError("Etude - 02");
            System.exit(1);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        return dir.delete();
    }

}
