//Lesson learnt: Don't play around with IDEA's ‘security’ remove!
//Otherwise, you'll have to use a decompiler to retrieve the code, like I did!
//RedreamR 24.11.8
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.luminosity.TweakProcess;
import team.rainfall.luminosity.TweakedClass;

public class Main {
    public static final String VERSION = "1.0.0";
    public static void main(String[] args) {
        System.out.println("Finality Framework Loader "+VERSION);
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        try {
            File overrideManifest = new File(args[1]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(overrideManifest.toPath()), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String[] var6 = sb.toString().split(";");
            int var7 = var6.length;
            int var8;
            for (var8 = 0; var8 < var7; ++var8) {
                String str = var6[var8];
                PluginManager.INSTANCE.findPlugins(new File(str));
            }

            TweakProcess process = new TweakProcess(PluginManager.INSTANCE.getPluginJarList(), new JarFile(new File("aoh3.exe")));
            process.targetFile = new File("aoh3.exe");
            process.tweak();
            classLoader.addUrl2((new File("aoh3.exe")).toURI().toURL());
            Iterator var25 = PluginManager.INSTANCE.getPluginFileList().iterator();

            while (var25.hasNext()) {
                File file = (File) var25.next();
                System.out.println(file.getName());
                classLoader.addUrl2(file.toURI().toURL());
            }

            if (args[0].equals("launch")) {
                process.tweakedClasses.forEach((tweakedClass) -> {;
                    classLoader.defineClass2(tweakedClass.className, tweakedClass.classBytes, 0, tweakedClass.classBytes.length);
                });
            }

            String[] var26 = sb.toString().split(";");
            var8 = var26.length;

            for (int var29 = 0; var29 < var8; ++var29) {
                String str = var26[var29];
                System.out.println(str);
                Field foldersAllListField = classLoader.loadClass("aoc.kingdoms.lukasz.jakowski.Steam.SteamManager").getField("modsFoldersAll");
                List<String> foldersAllList = (List) foldersAllListField.get((Object) null);
                Field foldersListField = classLoader.loadClass("aoc.kingdoms.lukasz.jakowski.Steam.SteamManager").getField("modsFolders");
                List<String> foldersList = (List) foldersListField.get((Object) null);
                Field foldersListSizeField = classLoader.loadClass("aoc.kingdoms.lukasz.jakowski.Steam.SteamManager").getField("modsFoldersSize");
                int folderListSize = foldersListSizeField.getInt((Object) null);
                foldersAllList.add(str);
                foldersList.add(str);
                foldersListSizeField.setInt((Object) null, folderListSize + 1);
            }

            switch (args[0]) {
                case "launch":
                    classLoader.loadClass("aoc.kingdoms.lukasz.jakowski.desktop.DesktopLauncher").getMethod("main", String[].class).invoke((Object) null, (Object) new String[0]);
                    break;
                case "gen":
                    for (TweakedClass tweakedClass : process.tweakedClasses) {
                        try {
                            deleteDir(new File("gen/"));
                            File file = new File("gen/" + tweakedClass.className.replace(".", "/") + ".class");
                            file.getParentFile().mkdirs();
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(tweakedClass.classBytes);
                        } catch (IOException var17) {
                            var17.printStackTrace();
                        }
                    }
            }

        } catch (Exception var18) {
            throw new RuntimeException(var18);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            String[] var2 = children;
            int var3 = children.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String child = var2[var4];
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        if (dir.delete()) {
            System.out.println("目录已被删除！");
            return true;
        } else {
            System.out.println("目录删除失败！");
            return false;
        }
    }
}
