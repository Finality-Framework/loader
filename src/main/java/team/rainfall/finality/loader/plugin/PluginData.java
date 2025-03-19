package team.rainfall.finality.loader.plugin;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.FinalityException;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * The PluginData class represents the data of a plugin, including its JAR file and manifest.
 * It is responsible for loading the plugin's JAR file and manifest from the specified file.
 *
 * @author RedreamR
 */
public class PluginData {

    public JarFile jarFile;
    public PluginManifest manifest = null;
    public File file;

    /**
     * Constructs a PluginData object with the specified file.
     * Loads the JAR file and manifest if the file is a valid JAR file.
     *
     * @param file the file representing the plugin
     * @throws RuntimeException if the file is not a valid JAR file
     * @author RedreamR
     */
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
