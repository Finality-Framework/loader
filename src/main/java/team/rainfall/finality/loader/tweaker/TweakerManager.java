package team.rainfall.finality.loader.tweaker;

import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.TweakManifest;
import team.rainfall.finality.loader.util.FinalityClassLoader;
import team.rainfall.luminosity.annotations.Tweak;

import java.util.ArrayList;

/**
 * The TweakerManager class manages a list of tweakers and provides methods to add tweakers from plugins and apply tweaks to class bytes.
 * It uses a singleton pattern to ensure a single instance of the manager.
 * The class loader is used to load tweaker classes dynamically.
 *
 * @author RedreamR
 */
public class TweakerManager {

    private final ArrayList<Tweaker> tweakers = new ArrayList<>();
    public FinalityClassLoader classLoader;
    public volatile static TweakerManager INSTANCE = new TweakerManager();
    public ArrayList<Tweaker> getTweakers() {
        return tweakers;
    }


    /**
     * Returns the list of tweakers.
     *
     * @author RedreamR
     */
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

    /**
     * Adds a tweaker from the given plugin data.
     *
     * @param classBytes the plugin data containing the tweaker information
     * @author RedreamR
     */
    public void tweak(String className, byte[] classBytes) {
        for (Tweaker tweaker : tweakers) {
            tweaker.transform(className, classBytes);
        }
    }

}
