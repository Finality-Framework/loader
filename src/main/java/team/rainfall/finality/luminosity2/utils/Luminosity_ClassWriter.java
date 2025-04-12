package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.ClassReader;

/**
 * A modified ClassWriter that allows us using a custom ClassLoader to avoid dependencies problem.
 * @author RedreamR
 */
public class Luminosity_ClassWriter extends org.objectweb.asm.ClassWriter {
    public static ClassLoader classLoader = null;
    public Luminosity_ClassWriter(int flags) {
        super(flags);
    }
    public Luminosity_ClassWriter(ClassReader classReader, int flags) {
        super(classReader,flags);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return classLoader;
    }
}
