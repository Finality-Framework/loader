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

/**
 * <p>The VdfManager class is responsible for managing and parsing the Steam VDF (Valve Data Format) files.</p>
 * <p>It provides methods to find the Steam installation path and parse the library folders VDF file<br>
 * to retrieve library paths.</p>
 * <p>This class is implemented as a singleton.</p>
 *
 * @author RedreamR
 */
public class VdfManager {

    public String steamPath = null;
    private static VdfManager INSTANCE = null;
    private ArrayList<String> libraryPaths = null;

    public VdfManager() {
    }

    /**
     * <p>Returns the singleton instance of the VdfManager class.</p>
     *
     * @return the singleton instance
     * @author RedreamR
     */
    public static VdfManager getINSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new VdfManager();
        }
        return INSTANCE;
    }

    /**
     * <p>Returns the list of library paths by parsing the Steam VDF file.</p>
     * <p>If the library paths are not already parsed, it will parse the VDF file.</p>
     *
     * @return the list of library paths
     * @author RedreamR
     */
    public ArrayList<String> getLibraryPaths() {
        if(libraryPaths == null){
            libraryPaths = new ArrayList<>();
            parseSteamVDF();
        }
        return libraryPaths;
    }


    /**
     * <p>Finds the Steam installation path by checking the Windows registry.</p>
     * <p>If the Steam installation path is found, it sets the steamPath field.</p>
     *
     * @author RedreamR
     */
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

    /**
     * <p>Parses the Steam VDF file to retrieve the library paths.</p>
     * <p>If the Steam installation path is not already set, it will find the Steam installation path first.</p>
     * <p>If the VDF file is found, it will parse the file and add the library paths to the libraryPaths list.</p>
     *
     * @author RedreamR
     */
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
