package team.rainfall.finality.loader.plugin;

/**
 * <p>The Plugin class represents a plugin that can be loaded by the application.
 * <p>It provides methods that are called when the application launches and shuts down.
 * <p>Subclasses can override these methods to provide custom behavior.
 *
 * @author RedreamR
 */
public class Plugin {

    /**
     * <p>Called when the application launches.
     * <p>Subclasses can override this method to provide custom behavior during launch.
     *
     * @author RedreamR
     */
    public void onLaunch(){}

    /**
     * <p>Called when the application shuts down.
     * <p>Subclasses can override this method to provide custom behavior during shutdown.
     *
     * @author RedreamR
     */
    public void onShutdown(){}
}
