//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.jar.JarFile;

public class PluginManager {
    public static PluginManager INSTANCE = new PluginManager();
    public ArrayList<File> pluginFileList = new ArrayList<>();

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
                        System.out.println("add plugin " + pluginFile.getName());
                        this.pluginFileList.add(pluginFile);
                    }
                }
            }
        }

    }

    public ArrayList<File> getPluginFileList() {
        return this.pluginFileList;
    }

    public ArrayList<JarFile> getPluginJarList() {
        ArrayList<JarFile> jarFiles = new ArrayList<>();

        for (File file : this.pluginFileList) {
            try {
                jarFiles.add(new JarFile(file));
            } catch (IOException var5) {
                throw new RuntimeException(var5);
            }
        }

        return jarFiles;
    }
}
