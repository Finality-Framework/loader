
package team.rainfall.finality.loader;

import java.io.*;
import java.util.*;

import com.formdev.flatlaf.FlatIntelliJLaf;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;

import javax.swing.plaf.PanelUI;

@SuppressWarnings("unused")
public class Main {
    public static final int TARGET_GAME_VERSION = 2013;
    public static final String CODENAME = "RAIDEN";
    public static final String VERSION = "1.4.4";
    public static final VersionType VERSION_TYPE = VersionType.RELEASE;
    public static final String STEAM_MANAGER_CLASS = "aoh.kingdoms.history.mainGame.Steam.SteamManager";
    public static String LAUNCHER_CLASS = "aoh.kingdoms.history.mainGame.desktop.DesktopLauncher";
    public static ArrayList<String> localMods = new ArrayList<>();
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

