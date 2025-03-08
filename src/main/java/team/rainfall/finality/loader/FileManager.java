//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import team.rainfall.finality.loader.gui.ErrorCode;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {

    public static FileManager INSTANCE = new FileManager();
    public FileManager() {
    }

    public File getSteamWSFolder() {
        File WSfolder = getSteamWSFolderDirect();
        if(WSfolder == null){
            WSfolder = getSteamWSFolderGlobal();
        }
        return WSfolder;
    }
    
    public File getSteamWSFolderDirect(){
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
            ArrayList<String> libraryPaths = new ArrayList<>();
            while (matcher.find()) {
                libraryPaths.add(matcher.group(1).replace("\\\\", "\\"));
            }
            // 添加默认Steam安装路径
            libraryPaths.add(steamPath);
            // 在每个库路径中查找Workshop文件夹
            for (String libraryPath : libraryPaths) {
                File workshopPath = new File(libraryPath, "steamapps/workshop/content/2772750");
                if (workshopPath.exists() && workshopPath.isDirectory()) {
                    return workshopPath;
                }
            }
            // 如果都没找到，抛出异常
            ErrorCode.showInternalError("Etude - 04");
            throw new RuntimeException("SteamWSFolder is missing");
            
        } catch (Exception e) {
            ErrorCode.showInternalError("Etude - 04");
            throw new RuntimeException("Failed to locate Steam Workshop folder: " + e.getMessage());
        }
    }

    private static String getLocalPath() {
        return (new File("")).getAbsolutePath() + File.separator;
    }
    
    public String[] getModsOffFile(){
        File file = new File("settings/ModsOff.txt");
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
    
    public String findGameFile(){
        File file = new File("aoh3.exe");
        if(file.exists()) return "aoh3.exe";
        file = new File("game.jar");
        if(file.exists()) return "game.jar";
        file = new File("aoh3.jar");
        if(file.exists()) return "aoh3.jar";
        file = new File("history2020.exe");
        if(file.exists()) return "history2020.exe";
        throw new RuntimeException("Can not found game file");
    }


}
