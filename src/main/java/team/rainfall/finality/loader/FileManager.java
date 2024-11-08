//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class FileManager {
    public static FileManager INSTANCE = new FileManager();
    private ArrayList<File> fallbackFolderList = new ArrayList();

    public FileManager() {
    }

    public void addFallbackFolder(File file) {
        if (file.isDirectory()) {
            this.fallbackFolderList.add(file);
        }

    }

    public File getResource(String path) {
        Iterator<File> var2 = this.fallbackFolderList.iterator();

        File resource;
        do {
            if (!var2.hasNext()) {
                resource = new File(getLocalPath(), path);
                if (resource.exists()) {
                    return resource;
                }

                return new File(getLocalPath(), path);
            }

            File file = (File)var2.next();
            resource = new File(file, path);
        } while(!resource.exists());

        return resource;
    }

    private static String getLocalPath() {
        return (new File("")).getAbsolutePath() + File.separator;
    }
}
