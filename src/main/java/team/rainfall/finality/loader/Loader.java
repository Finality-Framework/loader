package team.rainfall.finality.loader;

import com.formdev.flatlaf.FlatIntelliJLaf;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.installer.Installer;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static team.rainfall.finality.loader.Main.*;

public class Loader {
    static ParamParser paramParser = new ParamParser();
    static FinalityClassLoader classLoader;
    @SuppressWarnings("deprecated")
    public static void loaderMain(String[] args) {
        FinalityLogger.init();
        FinalityLogger.info("Finality Framework Loader " + VERSION);
        paramParser.parse(args);

        if(paramParser.mode == LaunchMode.INSTALL){
            Installer.install();
        }

        if(!paramParser.isReboot) {
            unstableWarn();
        }

        if(FileManager.parentFile != null){
            System.exit(dropAndLaunch(FileManager.parentFile,args));
        }

        FileUtil.createPrivateDir();
        FlatIntelliJLaf.setup();
        SplashScreen.create();
        if(GithubUtil.checkUpdate()){
            String [] options = {Localization.bundle.getString("update_now"),Localization.bundle.getString("later")};
            int i = JOptionPane.showOptionDialog(null, String.format(Localization.bundle.getString("new_version"), GithubUtil.latestVersion),Localization.bundle.getString("update"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
            if(i == 0){
                BrowserUtil.openUrl(GithubUtil.getLocaleRepoLink()+"/releases/");
                System.exit(0);
            }
        }
        long startTime = System.currentTimeMillis();
        classLoader = new FinalityClassLoader(new URL[0]);
        try (JarFile gameJar = new JarFile(new File(paramParser.gameFilePath))) {
            LAUNCHER_CLASS = gameJar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            //Search local mods
            for (File file : Objects.requireNonNull(FileManager.INSTANCE.getFile("mods").listFiles())) {
                String[] strings = FileManager.INSTANCE.getModsOffFile();
                List<String> list = Arrays.asList(strings);
                if (file.isDirectory() && !list.contains("mods/" + file.getName() + "/")) {
                    localMods.add("mods/" + file.getName() + "/");
                }
            }
            for (String str : localMods) {
                //Find plugins in local mods
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
            //Luminosity2
            LuminosityEnvironment environment = new LuminosityEnvironment(PluginManager.INSTANCE.pluginDataList, new File(paramParser.gameFilePath));
            tweak(environment);

            hijackSteamManager();

            if (paramParser.mode == LaunchMode.ONLY_GEN || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                for (ClassInfo classInfo : environment.classInfos) {
                    try {
                        deleteDir(FileManager.INSTANCE.getFile("gen/"));
                        File file = FileManager.INSTANCE.getFile("gen/" + classInfo.name.replace(".", "/") + ".class");
                        boolean ignored = file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(classInfo.bytes);
                        fos.close();
                    } catch (IOException var17) {

                        FinalityLogger.error("Gen err", var17);
                    }
                }
            }
            if (paramParser.mode == LaunchMode.ONLY_LAUNCH || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                FinalityLogger.info(String.format(Localization.bundle.getString("ready_to_launch"), (System.currentTimeMillis() - startTime)));
                try {
                    SplashScreen.destroy();
                    classLoader.loadClass(LAUNCHER_CLASS).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    FinalityLogger.error("Game err", e);
                    JOptionPane.showMessageDialog(null, Localization.bundle.getString("game_crashed"), Localization.bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception var18) {
            FinalityLogger.error("Unknown err", var18);
            ErrorCode.showInternalError("Etude - 01");
            System.exit(1);
        }
        SplashScreen.destroy();
    }

    static void unstableWarn(){
        if(VERSION_TYPE != VersionType.RELEASE) {
            FinalityLogger.warn(Localization.bundle.getString("unstable_tips_1"));
            FinalityLogger.warn(Localization.bundle.getString("unstable_tips_2"));
        }
    }


    /**
     * drop loader itself into the game folder,and execute it again to launch the game.<br/>
     * Note:Steam will block our launch if we try to launch game from the folder which is different from the game folder.
     * @param gamePath a folder file of game folder.
     * @return exit code
     * @author RedreamR
     */
    static int dropAndLaunch(File gamePath,String[] args){
        String currentJarPath = Loader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            currentJarPath = URLDecoder.decode(currentJarPath, "UTF-8");
            copyFile(new File(currentJarPath), new File(gamePath,"Finality_Loader.jar"));
            String[] param = {"java", "-jar", gamePath.getAbsolutePath()+"/"+"Finality_Loader.jar","-reboot"};
            param = ArrayUtil.mergeArrays(param,args);
            ProcessBuilder processBuilder = new ProcessBuilder(param);
            processBuilder.directory(gamePath);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            FinalityLogger.info("END OF EXECUTE,exit code:"+exitCode);
            return exitCode;
        } catch (Exception e) {
            FinalityLogger.error("WTF",e);
        }
        return 0;
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetFile);
             FileChannel sourceChannel = fis.getChannel();
             FileChannel targetChannel = fos.getChannel()) {

            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    //Hijack SteamManager to load mods only when Steam API is disabled.
    //But where is my Steam Workshop mods? To hell with those mods.
    private static void hijackSteamManager(){
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
    }

    private static void tweak(LuminosityEnvironment environment) throws MalformedURLException {
        environment.run();
        //Luminosity should run earlier than other classloader load
        if (paramParser.mode != LaunchMode.ONLY_GEN) {
            environment.load(classLoader);
        }
        for (PluginData data : PluginManager.INSTANCE.pluginDataList) {
            classLoader.addUrl2(data.file.toURI().toURL());
        }
        classLoader.addUrl2((new File(paramParser.gameFilePath)).toURI().toURL());

    }

}
