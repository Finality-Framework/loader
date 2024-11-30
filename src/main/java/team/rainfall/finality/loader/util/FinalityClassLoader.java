//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package team.rainfall.finality.loader.util;

import sun.misc.Resource;
import sun.misc.SharedSecrets;
import team.rainfall.finality.loader.tweaker.TweakerManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
        TweakerManager.INSTANCE.tweak(name,bytes);
        this.defineClass(name, bytes, off, len);
    }

    private boolean isSealed(String name, Manifest man) {
        Attributes attr = SharedSecrets.javaUtilJarAccess().getTrustedAttributes(man, name.replace('.', '/').concat("/"));
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Attributes.Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Attributes.Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    private Package getAndVerifyPackage(String pkgname, Manifest man, URL url) {
        Package pkg = getPackage(pkgname);
        if (pkg != null) {
            // Package found, so check package sealing.
            if (pkg.isSealed()) {
                // Verify that code source URL is the same.
                if (!pkg.isSealed(url)) {
                    throw new SecurityException("sealing violation: package " + pkgname + " is sealed");
                }
            } else {
                // Make sure we are not attempting to seal the package
                // at this code source URL.
                if ((man != null) && isSealed(pkgname, man)) {
                    throw new SecurityException("sealing violation: can't seal package " + pkgname + ": already loaded");
                }
            }
        }
        return pkg;
    }

    private void definePackageInternal(String pkgname, Manifest man, URL url) {
        if (getAndVerifyPackage(pkgname, man, url) == null) {
            try {
                if (man != null) {
                    definePackage(pkgname, man, url);
                } else {
                    definePackage(pkgname, null, null, null, null, null, null, null);
                }
            } catch (IllegalArgumentException iae) {
                // parallel-capable class loaders: re-verify in case of a
                // race condition
                if (getAndVerifyPackage(pkgname, man, url) == null) {
                    // Should never happen
                    throw new AssertionError("Cannot find package " +
                            pkgname);
                }
            }
        }
    }

    private Class<?> defineClass(String name, Resource res) throws IOException {
        long t0 = System.nanoTime();
        int i = name.lastIndexOf('.');
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            // Check if package already loaded.
            Manifest man = res.getManifest();
            definePackageInternal(pkgname, man, url);
        }
        // Now read the class bytes and define the class
        java.nio.ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            // Use (direct) ByteBuffer:
            TweakerManager.INSTANCE.tweak(name,bb.array());
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            sun.misc.PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
            return defineClass(name, bb, cs);
        } else {
            byte[] b = res.getBytes();
            TweakerManager.INSTANCE.tweak(name,b);
            // must read certificates AFTER reading bytes.
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            sun.misc.PerfCounter.getReadClassBytesTime().addElapsedTimeFrom(t0);
            return defineClass(name, b, 0, b.length, cs);
        }
    }

}
