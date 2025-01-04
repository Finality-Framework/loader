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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManager;
import team.rainfall.finality.loader.util.FinalityClassLoader;
import team.rainfall.luminosity.Plugin;
import team.rainfall.luminosity.TweakProcess;
import team.rainfall.luminosity.TweakedClass;

public class Main {
    public static final String VERSION = "1.1.0";
    public static final String STEAM_MANAGER_CLASS = "aoh.kingdoms.history.mainGame.Steam.SteamManager";
    public static final String LAUNCHER_CLASS = "aoh.kingdoms.history.mainGame.desktop.DesktopLauncher";
    public static void main(String[] args) {
        FinalityLogger.init();
        System.out.println("Finality Framework Loader " + VERSION);
        ParamParser paramParser = new ParamParser();
        paramParser.parse(args);
        long startTime = System.currentTimeMillis();
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        try {
            Manifest manifest = paramParser.manifest;
            //Search local mods
            for (File file : Objects.requireNonNull(new File("mods").listFiles())) {
                String[] strings = FileManager.INSTANCE.getModsOffFile();
                List<String> list = Arrays.asList(strings);
                if (file.isDirectory() && !list.contains("mods/" + file.getName() + "/")) {
                    manifest.localMods.add("mods/" + file.getName() + "/");
                }
            }
            for (String str : manifest.localMods) {
                //遍历str文件夹
                FinalityLogger.debug("DEBUG 2" + str);
                File file = new File(str);
                PluginManager.INSTANCE.findPlugins(file);
            }

            if (!paramParser.disableSteamAPI) {
                for (File file : Objects.requireNonNull(FileManager.INSTANCE.getSteamWSFolder().listFiles())) {
                    boolean shouldBreak = false;
                    for (String s : FileManager.INSTANCE.getModsOffFile()) {
                        FinalityLogger.debug("111 " + s + " 222 " + file.getAbsolutePath());
                        if (s.equals(file.getAbsolutePath())) {
                            FinalityLogger.debug("Found off mod " + s);
                            shouldBreak = true;
                            break;
                        }
                    }
                    if (shouldBreak) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        PluginManager.INSTANCE.findPlugins(file);
                    }
                }
            }
            //Luminosity Tweak
            TweakProcess process = new TweakProcess(PluginManager.INSTANCE.pluginDataList, new JarFile(new File(paramParser.gameFilePath)));
            process.targetFile = new File(paramParser.gameFilePath);
            process.tweak();
            for (PluginData data:PluginManager.INSTANCE.pluginDataList) {
                classLoader.addUrl2(data.file.toURI().toURL());
            }
            classLoader.addUrl2((new File(paramParser.gameFilePath)).toURI().toURL());
            if (paramParser.mode != LaunchMode.ONLY_GEN) {
                process.tweakedClasses.forEach((tweakedClass) ->
                {
                    FinalityLogger.debug("TWEAKED CLAZZ " + tweakedClass.className);
                    classLoader.defineClass2(tweakedClass.className, tweakedClass.classBytes, 0, tweakedClass.classBytes.length);
                });
            }
            //Luminosity Tweak end

            //Hijack SteamManager to load mods only when Steam API is disabled.
            //But where is my Steam Workshop mods? To hell with those mods.
            if (paramParser.disableSteamAPI) {
                for (String str : manifest.localMods) {
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
            if (paramParser.mode == LaunchMode.ONLY_GEN || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
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
            if (paramParser.mode == LaunchMode.ONLY_LAUNCH || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                FinalityLogger.info("Ready to launch game,time spending " + (System.currentTimeMillis() - startTime) + "ms");
                classLoader.loadClass(LAUNCHER_CLASS).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            }
        } catch (Exception var18) {
            throw new RuntimeException(var18);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            int var3 = 0;
            if (children != null) {
                var3 = children.length;
            }

            for (int var4 = 0; var4 < var3; ++var4) {
                String child = children[var4];
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

}
