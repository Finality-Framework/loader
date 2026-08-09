package team.rainfall.finality.loader;

import com.alibaba.fastjson2.JSON;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManager;
import team.rainfall.finality.loader.util.FileUtil;
import team.rainfall.finality.loader.util.ZipMerger;
import team.rainfall.finality.luminosity2.LuminosityEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Patch mode: generate a dex patch from plugin jars without GUI, VDF parsing or auto game discovery.<br/>
 * Reads patch.json (path can be specified via --patch-config), which contains the d8 path,
 * the game core path and the plugin paths. Produces generated.jar like ONLY_GEN mode,
 * then converts it to dex with d8.
 * @author RedreamR
 */
public class Patcher {
    private static final String DEFAULT_CONFIG = "patch.json";
    private static final String LUMINOSITY_JAR = "./.finality/luminosity2.jar";
    private static final String GENERATED_JAR = "./.finality/generated.jar";

    public static class PatchConfig {
        public String d8Path = null;
        //Path to android.jar,fallback to SDK auto-discovery when not set
        public String androidLibPath = null;
        public String gamePath = null;
        public List<String> plugins = null;
    }

    public static PatchConfig loadConfig(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            FinalityLogger.error("Patch config file not found: " + path);
            return null;
        }
        try {
            PatchConfig config = JSON.parseObject(FileUtil.readString_UTF8(file), PatchConfig.class);
            if (config == null) {
                FinalityLogger.error("Invalid patch config: " + path);
                return null;
            }
            return config;
        } catch (RuntimeException e) {
            FinalityLogger.error("Failed to parse patch config: " + path, e);
            return null;
        }
    }

    public static void patch(String configPath) {
        if (configPath == null || configPath.isEmpty()) {
            configPath = DEFAULT_CONFIG;
        }
        PatchConfig config = loadConfig(configPath);
        if (config == null) {
            System.exit(1);
        }
        if (config.gamePath == null || config.gamePath.isEmpty()) {
            FinalityLogger.error("gamePath is missing in patch config");
            System.exit(1);
        }
        if (config.d8Path == null || config.d8Path.isEmpty()) {
            FinalityLogger.error("d8Path is missing in patch config");
            System.exit(1);
        }

        File gameFile = new File(config.gamePath).getAbsoluteFile();
        if (!gameFile.exists() || !gameFile.isFile()) {
            FinalityLogger.error("Game core not found: " + config.gamePath);
            System.exit(1);
        }
        File d8Jar = new File(config.d8Path);
        if (!d8Jar.exists() || !d8Jar.isFile()) {
            FinalityLogger.error("d8.jar not found: " + config.d8Path);
            System.exit(1);
        }
        if (config.androidLibPath != null && !config.androidLibPath.isEmpty()) {
            File androidJar = new File(config.androidLibPath);
            if (!androidJar.exists() || !androidJar.isFile()) {
                FinalityLogger.error("android.jar not found: " + config.androidLibPath);
                System.exit(1);
            }
        }

        FinalityLogger.info("Starting patch, game: " + gameFile.getAbsolutePath());
        FileUtil.createPrivateDir();

        loadPlugins(config, configPath);

        //1.Luminosity2 processing,like ONLY_GEN mode
        LuminosityEnvironment environment = new LuminosityEnvironment(PluginManager.INSTANCE.pluginDataList, gameFile);
        try {
            environment.run();
        } finally {
            environment.dispose();
        }

        //2.Merge plugin jars and luminosity2.jar into generated.jar,like ONLY_GEN mode
        File l2File = new File(LUMINOSITY_JAR);
        if (!l2File.exists()) {
            FinalityLogger.error("Failed to produce " + LUMINOSITY_JAR);
            System.exit(1);
        }
        ArrayList<File> files = new ArrayList<>();
        for (PluginData pluginData : PluginManager.INSTANCE.pluginDataList) {
            files.add(pluginData.file);
        }
        files.add(l2File);
        File merged = new File(GENERATED_JAR);
        if (merged.exists()) {
            boolean ignored = merged.delete();
        } else {
            try {
                boolean ignored = merged.createNewFile();
            } catch (IOException e) {
                FinalityLogger.error("Failed to create " + GENERATED_JAR, e);
                System.exit(1);
            }
        }
        try {
            ZipMerger.mergeZipFiles(files.toArray(new File[0]), merged);
        } catch (IOException e) {
            FinalityLogger.error("Failed to merge jars", e);
            System.exit(1);
        }

        //3.Convert generated.jar to dex with d8
        try {
            Composer.ComposeConfig d8Config = new Composer.ComposeConfig();
            d8Config.d8Path = config.d8Path;
            d8Config.androidJar = config.androidLibPath;
            ArrayList<File> classpath = new ArrayList<>();
            classpath.add(gameFile);
            File dexZip = Composer.runD8(d8Config, merged, classpath);
            String baseName = gameFile.getName().endsWith(".jar")
                    ? gameFile.getName().substring(0, gameFile.getName().length() - 4)
                    : gameFile.getName();
            File dexOutput = new File(baseName + ".dex.zip");
            Files.move(dexZip.toPath(), dexOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
            boolean ignored = merged.delete();
            FinalityLogger.info("Patch completed: " + dexOutput.getAbsolutePath());
        } catch (Exception e) {
            FinalityLogger.error("Patch failed", e);
            System.exit(1);
        }
    }

    private static void loadPlugins(PatchConfig config, String configPath) {
        if (config.plugins == null) {
            return;
        }
        File configFile = new File(configPath);
        for (String pluginPath : config.plugins) {
            File file = new File(pluginPath);
            if (!file.exists()) {
                File resolved = new File(configFile.getParent(), pluginPath);
                if (!resolved.exists()) {
                    FinalityLogger.warn("Patch plugin not found: " + pluginPath);
                    continue;
                }
                file = resolved;
            }
            if (!file.isFile()) {
                FinalityLogger.warn("Patch plugin is not a file: " + pluginPath);
                continue;
            }
            try {
                PluginData data = new PluginData(file);
                if (PluginManager.INSTANCE.search(data.manifest.id) == null) {
                    PluginManager.INSTANCE.pluginDataList.add(data);
                    FinalityLogger.info("Loaded patch plugin: " + data.manifest.name + " (" + file.getName() + ")");
                } else {
                    FinalityLogger.info("Patch plugin already loaded, skipped: " + file.getName());
                }
            } catch (Exception e) {
                FinalityLogger.warn("Patch plugin is not a valid Finality plugin: " + file.getName());
            }
        }
    }

    private Patcher() {
    }
}
