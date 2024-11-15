//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class FileManager {
    public static FileManager INSTANCE = new FileManager();
    private ArrayList<File> fallbackFolderList = new ArrayList<>();
    public FileManager() {
    }
    public File getSteamWSFolder(){
        //获得当前目录父目录的父目录
        File file = new File("aoh3.exe").getAbsoluteFile();
        file = file.getParentFile().getParentFile().getParentFile();
        System.out.println(file.getAbsolutePath());
        file = new File(file,"workshop");
        file = new File(file,"content");
        file = new File(file,"2772750");
        if(file.exists()){
            return file;
        }
        throw new RuntimeException("Can not found SteamWSfolder");
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
