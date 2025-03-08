package team.rainfall.finality.loader.plugin;

import org.semver4j.Semver;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.Main;
import team.rainfall.finality.loader.util.FinalityException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.jar.JarFile;

public class PluginData {
    public JarFile jarFile;
    public PluginManifest manifest = null;
    public File file;
    public PluginData(File file) {
        this.file = file;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (file.getName().endsWith(".jar")) {
            try {
                this.jarFile = new JarFile(file);
                this.manifest = new PluginManifest(jarFile.getInputStream(jarFile.getJarEntry("plugin.json")));
            } catch (Exception e) {
                FinalityLogger.error("Failed to load plugin " + file.getName() + " because it is not a valid jar file",e);
                throw new FinalityException("Failed to load plugin " + file.getName() + " because it is not a valid jar file");
            }
        }
    }
}
