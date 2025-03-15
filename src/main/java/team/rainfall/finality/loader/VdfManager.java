package team.rainfall.finality.loader;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.util.Localization;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VdfManager {
    public String steamPath = null;
    private static VdfManager INSTANCE = null;
    private ArrayList<String> libraryPaths = null;
    public VdfManager() {
    }
    public static VdfManager getINSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new VdfManager();
        }
        return INSTANCE;
    }

    public ArrayList<String> getLibraryPaths() {
        if(libraryPaths == null){
            libraryPaths = new ArrayList<>();
            parseSteamVDF();
        }
        return libraryPaths;
    }
    public void findSteamInstallation(){
        if(Platform.isWindows()){
            String steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "Software\\Wow6432Node\\Valve\\Steam",
                    "InstallPath"
            );
            File file = new File(steamPath);
            if(file.exists())  this.steamPath = steamPath;
        }
    }
    public void parseSteamVDF(){
        libraryPaths.clear();
        if(steamPath == null){
            findSteamInstallation();
        }
        if(steamPath == null){
            FinalityLogger.error(Localization.bundle.getString("steam_not_found"));
            return;
        }
        try {
            File vdfFile = new File(steamPath, "config/libraryfolders.vdf");
            if (!vdfFile.exists()) {
                FinalityLogger.error(Localization.bundle.getString("vdf_file_not_found"));
                return;
            }
            String vdfContent = new String(Files.readAllBytes(vdfFile.toPath()), StandardCharsets.UTF_8);
            Pattern pathPattern = Pattern.compile("\"path\"\\s+\"([^\"]+)\"");
            Matcher matcher = pathPattern.matcher(vdfContent);
            while (matcher.find()) {
                libraryPaths.add(matcher.group(1).replace("\\\\", "\\"));
            }
            libraryPaths.add(steamPath);
        }catch (Exception e){
            FinalityLogger.error("Failed to parse Steam Config file",e);
        }
    }
}
