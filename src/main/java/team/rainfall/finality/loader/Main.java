//Lesson learnt: Don't play around with IDEA's ‘security’ remove!
//Otherwise, you'll have to use a decompiler to retrieve the code, like I did!
//RedreamR 24.11.8
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.luminosity.Plugin;
import team.rainfall.luminosity.TweakProcess;
import team.rainfall.luminosity.TweakedClass;

public class Main {
    public static final String VERSION = "1.0.3";
    public static final String STEAM_MANAGER_CLASS = "aoh.kingdoms.history.mainGame.Steam.SteamManager";
    public static final String LAUNCHER_CLASS = "aoh.kingdoms.history.mainGame.desktop.DesktopLauncher";
    public static void main(String[] args) {
        System.out.println("Finality Framework Loader " + VERSION);
        long startTime = System.currentTimeMillis();
        FinalityLogger.debug("START TIME "+startTime);
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        try {
            Manifest manifest;
            if(args.length > 1) {
                manifest = new Manifest(Files.newInputStream(Paths.get(args[1])));
            }else {
                manifest = new Manifest();
            }
            //Search local mods
            for(File file: Objects.requireNonNull(new File("mods").listFiles())){
                String[] strings = FileManager.INSTANCE.getModsOffFile();
                List<String> list = Arrays.asList(strings);
                list.forEach(System.out::println);
                if(file.isDirectory() && !list.contains("mods/"+file.getName()+"/")){
                    manifest.localMods.add("mods/"+file.getName()+"/");
                }
            }
            for(String str:manifest.localMods){
                //遍历str文件夹
                FinalityLogger.debug("DEBUG 2"+str);
                File file = new File(str);
                PluginManager.INSTANCE.findPlugins(file);
            }

            if(!manifest.disableSteamAPI){
                for(File file : Objects.requireNonNull(FileManager.INSTANCE.getSteamWSFolder().listFiles())){
                    boolean shouldBreak = false;
                    for(String s:FileManager.INSTANCE.getModsOffFile()){
                        FinalityLogger.debug("111 "+s+" 222 "+file.getAbsolutePath());
                        if (s.equals(file.getAbsolutePath())) {
                            FinalityLogger.debug("Found off mod "+s);
                            shouldBreak = true;
                            break;
                        }
                    }
                    if(shouldBreak){
                        continue;
                    }
                    if(file.isDirectory()){
                        PluginManager.INSTANCE.findPlugins(file);
                    }
                }
            }
            TweakProcess process = new TweakProcess(PluginManager.INSTANCE.getPluginFileList(), new JarFile(new File(manifest.gameFile)));
            process.targetFile = new File(manifest.gameFile);
            process.tweak();
            classLoader.addUrl2((new File(manifest.gameFile)).toURI().toURL());
            for (Plugin plugin: process.plugins) {
                classLoader.addUrl2(plugin.file.toURI().toURL());
            }
            if (args[0].equals("launch")) {
                process.tweakedClasses.forEach((tweakedClass) -> classLoader.defineClass2(tweakedClass.className, tweakedClass.classBytes, 0, tweakedClass.classBytes.length));
            }
            //Hijack SteamManager to load mods only when Steam API is disabled.
            //But where is my Steam Workshop mods? To hell with those mods.
            if (manifest.disableSteamAPI) {
                for (String str: manifest.localMods) {
                    Field foldersAllListField = classLoader.loadClass(STEAM_MANAGER_CLASS).getField("modsFoldersAll");
                    List<String> foldersAllList = (List) foldersAllListField.get(null);
                    Field foldersListField = classLoader.loadClass(STEAM_MANAGER_CLASS).getField("modsFolders");
                    List<String> foldersList = (List) foldersListField.get(null);
                    Field foldersListSizeField = classLoader.loadClass(STEAM_MANAGER_CLASS).getField("modsFoldersSize");
                    int folderListSize = foldersListSizeField.getInt(null);
                    foldersAllList.add(str);
                    foldersList.add(str);
                    foldersListSizeField.setInt(null, folderListSize + 1);
                }
            }
            switch (args[0]) {
                case "launch":
                    FinalityLogger.debug("END TIME "+System.currentTimeMillis()+",Time spending "+(System.currentTimeMillis()-startTime));
                    classLoader.loadClass(LAUNCHER_CLASS).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                    break;
                case "gen":
                    for (TweakedClass tweakedClass : process.tweakedClasses) {
                        try {
                            deleteDir(new File("gen/"));
                            File file = new File("gen/" + tweakedClass.className.replace(".", "/") + ".class");
                            boolean ignored = file.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(tweakedClass.classBytes);
                        } catch (IOException var17) {
                            var17.printStackTrace();
                        }
                    }
            }

        } catch (Exception var18) {
            throw new RuntimeException(var18);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            int var3 = children.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String child = children[var4];
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        if (dir.delete()) {
            System.out.println("目录已被删除！");
            return true;
        } else {
            System.out.println("目录删除失败！");
            return false;
        }
    }

    public static String getFallbackGameFilePath() {
        if (new File("aoh3.exe").exists()) {
            return "aoh3.exe";
        } else if (new File("game.jar").exists()) {
            return "game.jar";
        } else if (new File("aoh3.jar").exists()) {
            return "aoh3.jar";
        }
        return "history2020.exe";
    }
}
