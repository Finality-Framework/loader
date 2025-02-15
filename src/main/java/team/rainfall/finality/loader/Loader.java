package team.rainfall.finality.loader;

import com.formdev.flatlaf.FlatIntelliJLaf;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.gui.SplashScreen;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManager;
import team.rainfall.finality.loader.util.*;
import team.rainfall.finality.luminosity2.LuminosityEnvironment;
import team.rainfall.finality.luminosity2.utils.ClassInfo;
import team.rainfall.luminosity.TweakProcess;
import team.rainfall.luminosity.TweakedClass;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static team.rainfall.finality.loader.Main.*;

public class Loader {
    @SuppressWarnings("deprecated")
    public static void loaderMain(String[] args) {
        FinalityLogger.init();
        FinalityLogger.info("Finality Framework Loader " + VERSION);
        ParamParser paramParser = new ParamParser();
        paramParser.parse(args);
        FileUtil.createPrivateDir();
        FlatIntelliJLaf.install();
        SplashScreen.create();
        if(GithubUtil.checkUpdate()){
            String [] options = {Localization.bundle.getString("update_now"),Localization.bundle.getString("later")};
            int i = JOptionPane.showOptionDialog(null, String.format(Localization.bundle.getString("new_version"), GithubUtil.latestVersion),Localization.bundle.getString("update"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
            if(i == 0){
                BrowserUtil.openUrl(GithubUtil.getLocaleRepoLink()+"/releases/");
            }
        }
        long startTime = System.currentTimeMillis();
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        JarFile gameJar;
        try {
            gameJar = new JarFile(new File(paramParser.gameFilePath));
            LAUNCHER_CLASS = gameJar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            //Search local mods
            for (File file : Objects.requireNonNull(new File("mods").listFiles())) {
                String[] strings = FileManager.INSTANCE.getModsOffFile();
                List<String> list = Arrays.asList(strings);
                if (file.isDirectory() && !list.contains("mods/" + file.getName() + "/")) {
                    localMods.add("mods/" + file.getName() + "/");
                }
            }
            for (String str : localMods) {
                //遍历str文件夹
                File file = new File(str);
                PluginManager.INSTANCE.findPlugins(file);
            }

            if (!paramParser.disableSteamAPI) {
                for (File file : Objects.requireNonNull(FileManager.INSTANCE.getSteamWSFolder().listFiles())) {
                    boolean shouldBreak = false;
                    for (String s : FileManager.INSTANCE.getModsOffFile()) {
                        if (s.equals(file.getAbsolutePath())) {
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

            //Luminosity2
            LuminosityEnvironment environment = new LuminosityEnvironment(PluginManager.INSTANCE.pluginDataList,new File(paramParser.gameFilePath));
            environment.run();

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

                environment.load(classLoader);
            }
            //Luminosity Tweak end

            //Hijack SteamManager to load mods only when Steam API is disabled.
            //But where is my Steam Workshop mods? To hell with those mods.
            try {
                if (paramParser.disableSteamAPI) {
                    for (String str : localMods) {
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
            } catch (Exception ignored) {
            }
            if (paramParser.mode == LaunchMode.ONLY_GEN || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                for(ClassInfo classInfo : environment.classInfos){
                    try {
                        deleteDir(new File("gen/"));
                        File file = new File("gen/" + classInfo.name.replace(".", "/") + ".class");
                        boolean ignored = file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(classInfo.bytes);
                    } catch (IOException var17) {
                        FinalityLogger.error("Gen err",var17);
                    }
                }
                for (TweakedClass tweakedClass : process.tweakedClasses) {
                    try {
                        deleteDir(new File("gen/"));
                        File file = new File("gen/" + tweakedClass.className.replace(".", "/") + ".class");
                        boolean ignored = file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(tweakedClass.classBytes);
                    } catch (IOException var17) {
                        FinalityLogger.error("Gen err",var17);
                    }
                }
            }
            if (paramParser.mode == LaunchMode.ONLY_LAUNCH || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                FinalityLogger.info("Ready to launch game,time spending " + (System.currentTimeMillis() - startTime) + "ms");
                try {
                    SplashScreen.destroy();
                    classLoader.loadClass(LAUNCHER_CLASS).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                }catch (Exception e){
                    JOptionPane.showMessageDialog(null,Localization.bundle.getString("game_crashed"),Localization.bundle.getString("error"),JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception var18) {
            FinalityLogger.error("Unknown err",var18);
            ErrorCode.showInternalError("Etude - 01");
            System.exit(1);
        }
        SplashScreen.destroy();
    }
}
