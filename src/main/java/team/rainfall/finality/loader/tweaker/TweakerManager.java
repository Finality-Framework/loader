package team.rainfall.finality.loader.tweaker;

import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.TweakManifest;
import team.rainfall.finality.loader.util.FinalityClassLoader;
import team.rainfall.luminosity.annotations.Tweak;

import java.util.ArrayList;

public class TweakerManager {
    private ArrayList<Tweaker> tweakers = new ArrayList<>();
    public FinalityClassLoader classLoader;
    public volatile static TweakerManager INSTANCE = new TweakerManager();
    public ArrayList<Tweaker> getTweakers() {
        return tweakers;
    }

    public void addTweakerFromPlugin(PluginData data) {
        if (data.manifest.hasTweaker) {
            try {
                Tweaker tweaker = (Tweaker) classLoader.loadClass(data.manifest.tweaker).newInstance();
                tweaker.selfJar = data.jarFile;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void tweak(String className, byte[] classBytes) {
        for (Tweaker tweaker : tweakers) {
            tweaker.transform(className, classBytes);
        }
    }
}
