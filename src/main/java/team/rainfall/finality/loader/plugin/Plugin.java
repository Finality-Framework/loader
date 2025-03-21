package team.rainfall.finality.loader.plugin;

/**
 * <p>The Plugin class represents a plugin that can be loaded by the application.</p>
 * <p>It provides methods that are called when the application launches and shuts down.</p>
 * <p>Subclasses can override these methods to provide custom behavior.</p>
 *
 * @author RedreamR
 */
public class Plugin {

    /**
     * <p>Called when the application launches.</p>
     * <p>Subclasses can override this method to provide custom behavior during launch.</p>
     *
     * @author RedreamR
     */
    public void onLaunch(){}

    /**
     * <p>Called when the application shuts down.</p>
     * <p>Subclasses can override this method to provide custom behavior during shutdown.</p>
     *
     * @author RedreamR
     */
    public void onShutdown(){}
}
