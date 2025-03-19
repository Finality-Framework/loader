package team.rainfall.finality.loader.plugin;

/**
 * The Plugin class represents a plugin that can be loaded by the application.
 * It provides methods that are called when the application launches and shuts down.
 * Subclasses can override these methods to provide custom behavior.
 *
 * @author RedreamR
 */
public class Plugin {

    /**
     * Called when the application launches.
     * Subclasses can override this method to provide custom behavior during launch.
     */
    public void onLaunch(){}

    /**
     * Called when the application shuts down.
     * Subclasses can override this method to provide custom behavior during shutdown.
     */
    public void onShutdown(){}
}
