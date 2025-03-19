package team.rainfall.finality.loader;

/**
 * Enum representing the different launch modes for the application.
 *
 * @author RedreamR
 */
public enum LaunchMode {

    /**
     * Mode for installing the application.
     */
    INSTALL,

    /**
     * Mode for only launching the application.
     */
    ONLY_LAUNCH,

    /**
     * Mode for only generating necessary files.
     */
    ONLY_GEN,

    /**
     * Mode for both launching the application and generating necessary files.
     */
    LAUNCH_AND_GEN

}
