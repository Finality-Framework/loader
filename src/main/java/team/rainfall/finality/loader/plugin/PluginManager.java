//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader.plugin;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.Localization;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class PluginManager {
    public static PluginManager INSTANCE = new PluginManager();
    public ArrayList<PluginData> pluginDataList = new ArrayList<>();
    public PluginManager() {
    }
    public void findPlugins(File folder) {
        if (folder.isDirectory()) {
            File folder2 = new File(folder, "plugins");
            if (folder2.exists()) {
                FinalityLogger.debug("folder2 exists");
                File[] var3 = Objects.requireNonNull(folder2.listFiles());
                for (File pluginFile : var3) {
                    if (pluginFile.getName().endsWith(".jar")) {
                        FinalityLogger.info(String.format(Localization.bundle.getString("found_plugin"),pluginFile.getName()));
                        try{
                            PluginData data = new PluginData(pluginFile);
                            pluginDataList.add(data);
                        } catch (Exception e) {
                            FinalityLogger.error("Error loading plugin " + pluginFile.getName(), e);
                        }
                    }
                }
            }
        }

    }
}
