//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class FinalityClassLoader extends URLClassLoader {
    public FinalityClassLoader(URL[] urls) {
        super(urls);
    }

    public FinalityClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addUrl2(URL url) {
        this.addURL(url);
    }

    public void defineClass2(String name, byte[] bytes, int off, int len) {
        this.defineClass(name, bytes, off, len);
    }
}
