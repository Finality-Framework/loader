package team.rainfall.luminosity;

import java.net.URL;
import java.net.URLClassLoader;

public class DependenciesClassLoader extends URLClassLoader {
    public DependenciesClassLoader(URL[] urls) {
        super(urls);
    }
    public DependenciesClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    public void addUrl2(URL url){
        addURL(url);
    }
    public void defineClass2(String name,byte[] bytes,int off,int len){
        defineClass(name, bytes, off, len);
    }
}
