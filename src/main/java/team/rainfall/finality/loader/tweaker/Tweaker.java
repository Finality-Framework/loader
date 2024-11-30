package team.rainfall.finality.loader.tweaker;

import java.util.jar.JarFile;

public abstract class Tweaker {
    public JarFile selfJar;
    public abstract byte[] transform(String className,byte[] classBytes);
}
