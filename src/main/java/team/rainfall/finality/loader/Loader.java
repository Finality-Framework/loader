package team.rainfall.finality.loader;

import com.formdev.flatlaf.FlatIntelliJLaf;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.installer.Installer;
import team.rainfall.finality.loader.game.Product;
import team.rainfall.finality.loader.game.ProductDetector;
import team.rainfall.finality.loader.game.VersionDetector;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.gui.FinalityGUI;
import team.rainfall.finality.loader.gui.SplashScreen;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManager;
import team.rainfall.finality.loader.util.*;
import team.rainfall.finality.luminosity2.LuminosityEnvironment;

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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static team.rainfall.finality.loader.Main.*;

public class Loader {

    static ParamParser paramParser = new ParamParser();
    static FinalityClassLoader classLoader;
    static String[] args;
    static Product product = Product.AoH3;
    @SuppressWarnings("deprecated")
    public static void loaderMain(String[] args) {
        FinalityLogger.init();
        FinalityLogger.info("Finality Framework Loader " + VERSION);

        paramParser.parse(args);
        if(paramParser.gameFilePath == null){
            paramParser.gameFilePath = FileManager.INSTANCE.findGameFile();
        }
        try {
            JarFile jarFile = new JarFile(paramParser.gameFilePath);
            product = ProductDetector.detect(jarFile);
            jarFile.close();
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
        if(product == Product.AoH2){
            FinalityLogger.warn(Localization.bundle.getString("experimental_aoh2_support"));
        }
        sendDeviceInfo();
        FileUtil.createPrivateDir();
        FlatIntelliJLaf.setup();
        SplashScreen.create();
        checkUpdate();
        if(paramParser.mode == LaunchMode.INSTALL){
            Installer.install();
        }

        if(!paramParser.isReboot) {
            unstableWarn();
        }

        if(FileManager.parentFile != null){
            Loader.args = args;
            SplashScreen.destroy();
            FinalityGUI.main(args);
            return;

        }


        long startTime = System.currentTimeMillis();
        classLoader = new FinalityClassLoader(new URL[0]);
        try (JarFile gameJar = new JarFile(new File(paramParser.gameFilePath))) {
            LAUNCHER_CLASS = gameJar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            //checkGameVersion(new File(paramParser.gameFilePath));

            //Search local mods
            FileManager.INSTANCE.loadLocalMods();
            for (String str : FileManager.INSTANCE.localMods) {
                //Find plugins in local mods
                File file = new File(str);
                PluginManager.INSTANCE.findPlugins(file);
            }

            if (!paramParser.disableSteamAPI && product != Product.AoH2) {
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
            if(PluginManager.INSTANCE.search("team.rainfall.finality.api") == null){
                FinalityLogger.localizeError("finality_api_not_found");
                FinalityLogger.localizeError("subscribe_finality_api");
            }
            //Luminosity2
            LuminosityEnvironment environment = new LuminosityEnvironment(PluginManager.INSTANCE.pluginDataList, new File(paramParser.gameFilePath));
            tweak(environment);

            hijackSteamManager();

            if (paramParser.mode == LaunchMode.ONLY_GEN || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                File l2File = new File("./.finality/luminosity2.jar");
                ArrayList<File> files = new ArrayList<>();
                files.add(l2File);
                for (PluginData pluginData : PluginManager.INSTANCE.pluginDataList) {
                    files.add(pluginData.file);
                }
                File file2 = new File("./.finality/generated.jar");
                if (file2.exists()) {
                    boolean ignored = file2.delete();
                }else {
                    boolean ignored =  file2.createNewFile();
                }
                ZipMerger.mergeZipFiles(files.toArray(new File[0]), new File("./.finality/generated.jar"));
            }
            if (paramParser.mode == LaunchMode.ONLY_LAUNCH || paramParser.mode == LaunchMode.LAUNCH_AND_GEN) {
                FinalityLogger.info(String.format(Localization.bundle.getString("ready_to_launch"), (System.currentTimeMillis() - startTime)));
                try {
                    SplashScreen.destroy();
                    environment.dispose();
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

    public static void liteLaunch(){
        dropAndLaunch(FileManager.parentFile, args);
    }
    /**
     * drop loader itself into the game folder,and execute it again to launch the game.<br/>
     * Note:Steam will block our launch if we try to launch game from the folder which is different from the game folder.
     *
     * @param gamePath a folder file of game folder.
     * @author RedreamR
     */
    static void dropAndLaunch(File gamePath, String[] args){
        SplashScreen.destroy();
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
        } catch (Exception e) {
            FinalityLogger.error("WTF",e);
        }
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
    @SuppressWarnings("unchecked")
    private static void hijackSteamManager(){
        try {
            if (paramParser.disableSteamAPI && product != Product.AoH2) {
                for (String str : FileManager.INSTANCE.localMods) {
                    Field foldersAllListField = classLoader.loadClass(STEAM_MANAGER_CLASS).getField("modsFoldersAll");
                    List<String> foldersAllList = (List<String>) foldersAllListField.get(null);
                    Field foldersListField = classLoader.loadClass(STEAM_MANAGER_CLASS).getField("modsFolders");
                    List<String> foldersList = (List<String>) foldersListField.get(null);
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
    private static void checkUpdate(){
        if(GithubUtil.checkUpdate()){
            String [] options = {Localization.bundle.getString("update_now"),Localization.bundle.getString("later")};
            int i = JOptionPane.showOptionDialog(null, String.format(Localization.bundle.getString("new_version"), GithubUtil.latestVersion),Localization.bundle.getString("update"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
            if(i == 0){
                BrowserUtil.openUrl(GithubUtil.getLocaleRepoLink()+"/releases/");
                System.exit(0);
            }
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
    private static void sendDeviceInfo(){
        FinalityLogger.info("OS Info: "+OSUtil.getSystem());
        FinalityLogger.info("Java Info: "+OSUtil.getJavaInfo());
        FinalityLogger.info("CPU ID: "+OSUtil.getProcessorIdentifier());
        FinalityLogger.info("CPU NAME: "+OSUtil.getCpuName());
    }
    @SuppressWarnings("unused")
    private static void checkGameVersion(File file) throws IOException, NoSuchAlgorithmException {
        VersionDetector.detector.loadVersions();
        byte[] hash = FileUtil.calculateSHA256(file);
        String hashStr = StringUtil.bytesToHex(hash);
        FinalityLogger.info(String.format(Localization.bundle.getString("sha256_of_the_game"), hashStr));
        if(VersionDetector.detector.isKnownVersion(hashStr)){
            FinalityLogger.info(String.format(Localization.bundle.getString("version_of_game"), VersionDetector.detector.getVersionID(hashStr)));
            FinalityLogger.info(String.format(Localization.bundle.getString("version_desc"), VersionDetector.detector.getVersionDesc(hashStr)));
            if(VersionDetector.detector.getVersionID(hashStr) != TARGET_GAME_VERSION){
                FinalityLogger.warn(Localization.bundle.getString("unsupported_version"));
            }
        }else {
            FinalityLogger.warn(Localization.bundle.getString("unknown_version"));
        }
    }

}
