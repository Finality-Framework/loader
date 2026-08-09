package team.rainfall.finality.loader;

import com.alibaba.fastjson2.JSON;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManager;
import team.rainfall.finality.loader.util.FileUtil;
import team.rainfall.finality.loader.util.Localization;
import team.rainfall.finality.loader.util.ZipMerger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Composer {
    private static final String LUMINOSITY_JAR = "./.finality/luminosity2.jar";
    private static final List<File> rawPlugins = new ArrayList<>();

    public static class ComposeConfig {
        public String gamePath = null;
        public String d8Path = null;
        public String androidJar = null;
        public List<String> plugins = null;
        public List<String> excludedClasses = null;
    }

    public static ComposeConfig loadConfig(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            FinalityLogger.error("Compose config file not found: " + path);
            return null;
        }
        try {
            ComposeConfig config = JSON.parseObject(FileUtil.readString_UTF8(file), ComposeConfig.class);
            if (config == null) {
                FinalityLogger.error("Invalid compose config: " + path);
                return null;
            }
            return config;
        } catch (RuntimeException e) {
            FinalityLogger.error("Failed to parse compose config: " + path, e);
            return null;
        }
    }

    public static void loadExtraPlugins(ComposeConfig config) {
        if (config.plugins == null) {
            return;
        }
        for (String pluginPath : config.plugins) {
            File file = new File(pluginPath);
            if (!file.exists()) {
                File resolved = new File(new File(Loader.paramParser.composeConfigPath).getParent(), pluginPath);
                if (!resolved.exists()) {
                    FinalityLogger.warn("Compose plugin not found: " + pluginPath);
                    continue;
                }
                file = resolved;
            }
            if (!file.isFile()) {
                FinalityLogger.warn("Compose plugin is not a file: " + pluginPath);
                continue;
            }
            try {
                PluginData data = new PluginData(file);
                if (PluginManager.INSTANCE.search(data.manifest.id) == null) {
                    PluginManager.INSTANCE.pluginDataList.add(data);
                    FinalityLogger.info("Loaded compose plugin: " + data.manifest.name + " (" + file.getName() + ")");
                } else {
                    FinalityLogger.info("Compose plugin already loaded, skipped: " + file.getName());
                }
            } catch (Exception e) {
                FinalityLogger.warn("Compose plugin is not a valid Finality plugin, it will be merged as a raw jar: " + file.getName());
                rawPlugins.add(file);
            }
        }
    }

    public static void compose(ComposeTarget target, ComposeConfig config) {
        if (config.gamePath == null || config.gamePath.isEmpty()) {
            FinalityLogger.error("gamePath is missing in compose config");
            System.exit(1);
        }
        File gameFile = new File(config.gamePath);
        if (!gameFile.exists() || !gameFile.isFile()) {
            FinalityLogger.error("Game jar not found: " + config.gamePath);
            System.exit(1);
        }
        FinalityLogger.info("Starting compose, target: " + target.name().toLowerCase() + ", game: " + gameFile.getAbsolutePath());

        String[] excluded = config.excludedClasses == null ? new String[0] : config.excludedClasses.toArray(new String[0]);
        if (excluded.length > 0) {
            FinalityLogger.info("Excluded class prefixes: " + String.join(", ", config.excludedClasses));
        }

        ArrayList<File> sources = new ArrayList<>();
        sources.add(gameFile);
        for (PluginData pluginData : PluginManager.INSTANCE.pluginDataList) {
            sources.add(pluginData.file);
        }
        sources.addAll(rawPlugins);
        sources.add(new File(LUMINOSITY_JAR));

        String baseName = gameFile.getName().endsWith(".jar")
                ? gameFile.getName().substring(0, gameFile.getName().length() - 4)
                : gameFile.getName();

        try {
            if (target == ComposeTarget.ANDROID) {
                File merged = new File("./.finality/composed-merged.jar");
                try {
                    if (merged.exists()) {
                        boolean ignored = merged.delete();
                    } else {
                        boolean ignored = merged.createNewFile();
                    }
                    ZipMerger.mergeZipFiles(sources.toArray(new File[0]), merged, excluded);
                } catch (IOException e) {
                    FinalityLogger.error("Failed to merge jars", e);
                    System.exit(1);
                }
                File dexZip = runD8(config, merged, null);
                File dexOutput = new File(gameFile.getParentFile(), baseName + ".dex.zip");
                Files.move(dexZip.toPath(), dexOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
                boolean ignored = merged.delete();
                FinalityLogger.info("Compose completed: " + dexOutput.getAbsolutePath());
            } else {
                File output = new File(gameFile.getParentFile(), baseName + ".composed.jar");
                ZipMerger.mergeZipFiles(sources.toArray(new File[0]), output, excluded);
                rewriteManifestPreservingGame(output, gameFile);
                FinalityLogger.info("Compose completed: " + output.getAbsolutePath());
            }
        } catch (Exception e) {
            FinalityLogger.error("Compose failed", e);
            System.exit(1);
        }
    }

    static File runD8(ComposeConfig config, File merged, List<File> classpath) throws Exception {
        if (config.d8Path == null || config.d8Path.isEmpty()) {
            FinalityLogger.error(Localization.bundle.getString("compose_d8_missing"));
            System.exit(1);
        }
        File d8Jar = new File(config.d8Path);
        if (!d8Jar.exists() || !d8Jar.isFile()) {
            FinalityLogger.error("d8.jar not found: " + config.d8Path);
            System.exit(1);
        }

        File dexZip = new File("./.finality/dex.zip");
        if (dexZip.exists()) {
            boolean ignored = dexZip.delete();
        }

        FinalityLogger.info("Running d8 to generate dex files...");
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(d8Jar.getAbsolutePath());
        command.add("com.android.tools.r8.D8");
        command.add("--release");
        command.add("--min-api");
        command.add("21");
        command.add("--output");
        command.add(dexZip.getAbsolutePath());
        String androidJar = resolveAndroidJar(config, d8Jar);
        if (androidJar != null) {
            command.add("--lib");
            command.add(androidJar);
        } else {
            FinalityLogger.warn(Localization.bundle.getString("compose_android_jar_not_found"));
        }
        if (classpath != null) {
            for (File file : classpath) {
                if (file.exists() && file.isFile()) {
                    command.add("--classpath");
                    command.add(file.getAbsolutePath());
                }
            }
        }
        command.add(merged.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            FinalityLogger.error("d8 exited with code " + exitCode);
            System.exit(1);
        }
        if (!dexZip.exists()) {
            FinalityLogger.error("d8 produced no dex output");
            System.exit(1);
        }
        int dexCount = countDexEntries(dexZip);
        if (dexCount == 0) {
            FinalityLogger.error("d8 produced no dex files");
            System.exit(1);
        }
        FinalityLogger.info("d8 finished, dex files: " + dexCount);
        return dexZip;
    }

    private static void rewriteManifestPreservingGame(File output, File gameJarFile) throws IOException {
        Manifest gameManifest;
        try (JarFile jf = new JarFile(gameJarFile)) {
            gameManifest = jf.getManifest();
        }
        if (gameManifest == null) {
            return;
        }
        File temp = new File(output.getAbsolutePath() + ".tmp");
        if (temp.exists()) {
            boolean ignored = temp.delete();
        }
        try (JarInputStream jis = new JarInputStream(new FileInputStream(output));
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(temp))) {
            jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            gameManifest.write(jos);
            jos.closeEntry();
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = jis.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name.equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                    continue;
                }
                jos.putNextEntry(new ZipEntry(name));
                int len;
                while ((len = jis.read(buffer)) > 0) {
                    jos.write(buffer, 0, len);
                }
                jos.closeEntry();
                jis.closeEntry();
            }
        }
        Files.move(temp.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static int countDexEntries(File zipFile) {
        int count = 0;
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().endsWith(".dex")) {
                    count++;
                }
            }
        } catch (IOException e) {
            FinalityLogger.error("Failed to read d8 output", e);
        }
        return count;
    }

    private static String resolveAndroidJar(ComposeConfig config, File d8Jar) {
        if (config.androidJar != null && !config.androidJar.isEmpty()) {
            return config.androidJar;
        }
        File sdkRoot = d8Jar.getParentFile().getParentFile().getParentFile().getParentFile();
        File platforms = new File(sdkRoot, "platforms");
        if (platforms.exists() && platforms.isDirectory()) {
            File[] dirs = platforms.listFiles(File::isDirectory);
            if (dirs != null) {
                String best = null;
                int bestApi = -1;
                for (File dir : dirs) {
                    try {
                        int api = Integer.parseInt(dir.getName().replace("android-", ""));
                        if (api > bestApi) {
                            bestApi = api;
                            best = dir.getAbsolutePath();
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (best != null) {
                    File androidJar = new File(best, "android.jar");
                    if (androidJar.exists()) {
                        return androidJar.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
}
