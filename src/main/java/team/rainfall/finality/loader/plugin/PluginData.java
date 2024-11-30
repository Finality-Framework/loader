package team.rainfall.finality.loader.plugin;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.FinalityException;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class PluginData {
    public JarFile jarFile = null;
    public PluginManifest manifest = null;
    public File file = null;
    public boolean isOldPlugin = true;
    public PluginData(File file) {
        this.file = file;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TweakManifest tweakManifest;
        if (file.getName().endsWith(".jar")) {
            try{
                this.isOldPlugin = jarFile.getJarEntry("modify.json") != null;
                if(isOldPlugin){
                    tweakManifest = new TweakManifest(jarFile.getInputStream(jarFile.getEntry("modify.json")));
                    this.manifest = buildDefaultManifestForOldPlugin(tweakManifest);
                    FinalityLogger.warn("Found legacy plugin "+tweakManifest.packageName+",we recommend to update this plugin.");
                }
            } catch (Exception e) {
                FinalityLogger.debug("Why there is a exception??? "+e.getMessage());
                this.isOldPlugin = false;
            }
            try {
                this.jarFile = new JarFile(file);
                if(!isOldPlugin) {
                    this.manifest = new PluginManifest(jarFile.getInputStream(jarFile.getJarEntry("plugin.json")));
                }
            } catch (Exception e) {
                FinalityLogger.error("Failed to load plugin " + file.getName() + " because it is not a valid jar file",e);
                throw new FinalityException("Failed to load plugin " + file.getName() + " because it is not a valid jar file");
            }
        }
    }
    public static PluginManifest buildDefaultManifestForOldPlugin(TweakManifest tweakManifest){
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.id = tweakManifest.packageName;
        pluginManifest.hasTweaker = false;
        pluginManifest.version = 0;
        pluginManifest.useLuminosity = true;
        pluginManifest.sdkVersion = 2;
        return pluginManifest;
    }
}
