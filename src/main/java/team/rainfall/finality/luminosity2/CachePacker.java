package team.rainfall.finality.luminosity2;

import team.rainfall.finality.luminosity2.utils.ClassInfo;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * CachePacker is a utility class that packs a list of class files into a JAR file.<br/>
 * Then,the jar file can be loaded by a {@link URLClassLoader}.
 * @see LuminosityEnvironment
 * @author RedreamR
 */
public class CachePacker {
    public static void packClassesIntoJar(List<ClassInfo> classInfoList, String jarFilePath) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Created-By", "Finality Loader Luminosity2");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jarFilePath)), manifest)) {
            for (ClassInfo classInfo : classInfoList) {
                String path = classInfo.name.replace('.', '/') + ".class";
                JarEntry entry = new JarEntry(path);
                jos.putNextEntry(entry);
                jos.write(classInfo.bytes);
                jos.closeEntry();
            }
        }
    }
}
