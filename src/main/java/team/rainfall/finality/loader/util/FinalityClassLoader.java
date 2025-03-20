//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader.util;

import team.rainfall.finality.loader.tweaker.TweakerManager;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>A custom class loader that extends URLClassLoader to provide additional functionality.
 * <p>This class loader allows adding URLs and defining classes with byte arrays.
 *
 * @author RedreamR
 */
public class FinalityClassLoader extends URLClassLoader {

    /**
     * <p>Constructs a new FinalityClassLoader for the specified URLs.
     *
     * @param urls the URLs from which to load classes and resources
     * @author RedreamR
     */
    public FinalityClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * <p>Constructs a new FinalityClassLoader for the specified URLs and parent class loader.
     *
     * @param urls   the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @author RedreamR
     */
    public FinalityClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * <p>Adds a new URL to the list of URLs to search for classes and resources.
     *
     * @param url the URL to be added
     * @author RedreamR
     */
    public void addUrl2(URL url) {
        this.addURL(url);
    }


    /**
     * <p>Defines a new class with the specified name and byte array.
     * <p>This method also applies tweaks using the TweakerManager.
     *
     * @param name  the name of the class
     * @param bytes the byte array containing the class data
     * @param off   the start offset in the byte array
     * @param len   the length of the class data
     * @author RedreamR
     */
    public void defineClass2(String name, byte[] bytes, int off, int len) {
        TweakerManager.INSTANCE.tweak(name,bytes);
        this.defineClass(name, bytes, off, len);
    }

}
