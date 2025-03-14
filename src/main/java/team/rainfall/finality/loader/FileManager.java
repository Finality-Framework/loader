//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import team.rainfall.finality.loader.util.Localization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {
    public static File parentFile = null;
    public static FileManager INSTANCE = new FileManager();
    ArrayList<String> libraryPaths = new ArrayList<>();
    public FileManager() {
    }
    public File getFile(String path){
        return new File(path);
    }
    public void parseSteamVDF(){
        try {
            libraryPaths.clear();
            // 从注册表获取Steam安装路径
            String steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    "Software\\Wow6432Node\\Valve\\Steam",
                    "InstallPath"
            );
            // 读取libraryfolders.vdf文件
            File vdfFile = new File(steamPath, "config/libraryfolders.vdf");
            if (!vdfFile.exists()) {
                ErrorCode.showInternalError("Etude - 04");
                throw new RuntimeException("Steam library config not found");
            }

            String vdfContent = new String(Files.readAllBytes(vdfFile.toPath()), StandardCharsets.UTF_8);

            // 解析VDF文件查找所有库路径
            Pattern pathPattern = Pattern.compile("\"path\"\\s+\"([^\"]+)\"");
            Matcher matcher = pathPattern.matcher(vdfContent);
            while (matcher.find()) {
                libraryPaths.add(matcher.group(1).replace("\\\\", "\\"));
            }
            // 添加默认Steam安装路径
            libraryPaths.add(steamPath);
        }catch (Exception e){
            FinalityLogger.error("Failed to parse Steam Config file",e);
        }
    }

    public File getSteamWSFolder() {
        File WSfolder = getSteamWSFolderDirectly();
        if(WSfolder == null){
            WSfolder = getSteamWSFolderGlobal();
        }
        return WSfolder;
    }
    
    public File getSteamWSFolderDirectly(){
        //获得当前目录父目录的父目录
        File file = new File("aoh3.exe").getAbsoluteFile();
        file = file.getParentFile().getParentFile().getParentFile();
        file = new File(file,"workshop");
        file = new File(file,"content");
        file = new File(file,"2772750");
        if(file.exists()){
            return file;
        }
        return null;
    }

    public File getSteamWSFolderGlobal() {
        try {
            for (String libraryPath : libraryPaths) {
                File workshopPath = new File(libraryPath, "steamapps/workshop/content/2772750");
                if (workshopPath.exists() && workshopPath.isDirectory()) {
                    return workshopPath;
                }
            }

            ErrorCode.showInternalError("Etude - 04");
            throw new RuntimeException("SteamWSFolder is missing");
            
        } catch (Exception e) {
            ErrorCode.showInternalError("Etude - 04");
            throw new RuntimeException("Failed to locate Steam Workshop folder: " + e.getMessage());
        }
    }

    public String[] getModsOffFile(){
        File file = getFile("settings/ModsOff.txt");
        if(file.exists()){
            //将文件内的数据读入String
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String content = new String(bytes, StandardCharsets.UTF_8);
                return content.split(";");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new String[0];
    }

    /**
     * Try to find game core file by parsing Steam VDF File.
     * @author RedreamR
     */
    public void findGameFileByVDF(){
        FinalityLogger.info(Localization.bundle.getString("find_game_core_by_vdf"));
        for (String libraryPath : libraryPaths) {
            File gameFolder = new File(libraryPath, "steamapps/common/Age of History 3");
            if (gameFolder.exists() && gameFolder.isDirectory()) {
                FinalityLogger.info(String.format(Localization.bundle.getString("found_game_core_by_vdf"), gameFolder.getAbsoluteFile().getAbsolutePath()));
                parentFile = gameFolder;
                return;
            }
        }
        ErrorCode.showInternalError("Etude - 03");
        throw new RuntimeException("Can not found game file");
    }
    public String findGameFile(){
        File file = new File("aoh3.exe");
        if(file.exists()) return "aoh3.exe";
        file = new File("game.jar");
        if(file.exists()) return "game.jar";
        file = new File("aoh3.jar");
        if(file.exists()) return "aoh3.jar";
        file = new File("history2020.exe");
        if(file.exists()) return "history2020.exe";
        findGameFileByVDF();
        return "";
    }
    public File findGameFile2(File folder){
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().equals("aoh3.exe") || file.getName().equals("game.jar") || file.getName().equals("aoh3.jar") || file.getName().equals("history2020.exe"))
                return file;
        }
        return null;
    }
}
