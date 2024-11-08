//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.jar.JarFile;

public class PluginManager {
    public static PluginManager INSTANCE = new PluginManager();
    public ArrayList<File> pluginFileList = new ArrayList();

    public PluginManager() {
    }

    public void findPlugins(File folder) {
        if (folder.isDirectory()) {
            File folder2 = new File(folder, "plugins");
            if (folder2.exists()) {
                File[] var3 = (File[])Objects.requireNonNull(folder2.listFiles());
                int var4 = var3.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    File pluginFile = var3[var5];
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
        ArrayList<JarFile> jarFiles = new ArrayList();
        Iterator var2 = this.pluginFileList.iterator();

        while(var2.hasNext()) {
            File file = (File)var2.next();

            try {
                jarFiles.add(new JarFile(file));
            } catch (IOException var5) {
                IOException e = var5;
                throw new RuntimeException(e);
            }
        }

        return jarFiles;
    }
}
