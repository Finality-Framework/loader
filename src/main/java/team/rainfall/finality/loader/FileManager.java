//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.util.Localization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class FileManager {
    //The folder of game core
    public static File parentFile = null;
    public static FileManager INSTANCE = new FileManager();
    public ArrayList<String> localMods = new ArrayList<>();
    public FileManager() {
    }
    public void loadLocalMods(){
        for (File file : Objects.requireNonNull(FileManager.INSTANCE.getFile("mods").listFiles())) {
            String[] strings = FileManager.INSTANCE.getModsOffFile();
            List<String> list = Arrays.asList(strings);
            if (file.isDirectory() && !list.contains("mods/" + file.getName() + "/")) {
                localMods.add("mods/" + file.getName() + "/");
            }
        }
    }
    public File getFile(String path) {
        return new File(path);
    }

    public File getSteamWSFolder() {
        File WSfolder = getSteamWSFolderDirectly();
        if (WSfolder == null) {
            WSfolder = getSteamWSFolderGlobal();
        }
        return WSfolder;
    }

    public File getSteamWSFolderDirectly() {
        //获得当前目录父目录的父目录
        File file = new File("aoh3.exe").getAbsoluteFile();
        file = file.getParentFile().getParentFile().getParentFile();
        file = new File(file, "workshop");
        file = new File(file, "content");
        file = new File(file, "2772750");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public File getSteamWSFolderGlobal() {
        try {
            for (String libraryPath : VdfManager.getINSTANCE().getLibraryPaths()) {
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

    public String[] getModsOffFile() {
        File file = getFile("settings/ModsOff.txt");
        if (file.exists()) {
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
    public void findGameFileByVDF() {

        FinalityLogger.info(Localization.bundle.getString("find_game_core_by_vdf"));
        for (String libraryPath : VdfManager.getINSTANCE().getLibraryPaths()) {
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

    public String findGameFile() {
        File file = new File("aoh3.exe");
        if (file.exists()) return "aoh3.exe";
        file = new File("game.jar");
        if (file.exists()) return "game.jar";
        file = new File("aoh3.jar");
        if (file.exists()) return "aoh3.jar";
        file = new File("history2020.exe");
        if (file.exists()) return "history2020.exe";
        if (!Loader.paramParser.forceNoVDF) {
            findGameFileByVDF();
        }
        return "";
    }
}
