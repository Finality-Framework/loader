package team.rainfall.finality.loader.tweaker;

import java.util.jar.JarFile;

/**
 * <p>T The Tweaker class is an abstract class that represents a tweaker.</p>
 * <p>TIt contains a reference to the JAR file of the tweaker and an abstract method for transforming class bytes.</p>
 * <p>TSubclasses should implement the transform method to provide specific transformation logic.</p>
 *
 * @author RedreamR
 */
public abstract class Tweaker {

    public JarFile selfJar;

    /**
     * <p>TTransforms the given class bytes.
     *
     * @param className the name of the class to transform
     * @param classBytes the original class bytes
     * @return the transformed class bytes
     * @author RedreamR
     */
    public abstract byte[] transform(String className,byte[] classBytes);

}
