package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {
    public static ClassNode getClassFromJar(JarFile file, String path) throws FileNotFoundException {
        path = path.replace(".", "/");
        path = path + ".class";
        //FinalityLogger.info(path);
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().equalsIgnoreCase(path)) {
                ClassReader reader = null;
                try {
                    reader = new ClassReader(file.getInputStream(entry));
                } catch (IOException e) {
                    throw new FileNotFoundException("File not found: " + path);
                }
                ClassNode node = new ClassNode();
                reader.accept(node, 0);
                return node;
            }
        }
        throw new FileNotFoundException("File not found: " + path);
    }
}
