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

/**
 * The Main class serves as the entry point for the application.
 * It initializes the application and handles any uncaught exceptions.
 *
 * @author RedreamR
 */
@SuppressWarnings("unused")
public class Main {

    public static final String CODENAME = "RAIDEN";
    public static final String VERSION = "1.3.2";
    public static final VersionType VERSION_TYPE = VersionType.RELEASE;
    public static final String STEAM_MANAGER_CLASS = "aoh.kingdoms.history.mainGame.Steam.SteamManager";
    public static String LAUNCHER_CLASS = "aoh.kingdoms.history.mainGame.desktop.DesktopLauncher";
    public static ArrayList<String> localMods = new ArrayList<>();

    /**
     * The main method serves as the entry point for the application.
     * It sets up the FlatIntelliJLaf look and feel and calls the loaderMain method.
     *
     * @param args the command line arguments
     * @author Greyeon, RedreamR
     */
    public static void main(String[] args) {
        try {
            FlatIntelliJLaf.setup();
            Loader.loaderMain(args);
        }catch (Throwable e){
            FinalityLogger.error("Unknown err",e);
            ErrorCode.showInternalError("Etude - 02");
            System.exit(1);
        }
    }

    /**
     * Deletes the specified directory and all its contents.
     *
     * @param dir the directory to delete
     * @return true if the directory was successfully deleted, false otherwise
     * @author Greyeon, RedreamR
     */
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

