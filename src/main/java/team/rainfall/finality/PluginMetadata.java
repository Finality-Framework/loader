package team.rainfall.finality;

/**
 * Metadata of a Plugin,used by the generated {@code PluginsCollection} class.
 * @author RedreamR
 */
public class PluginMetadata {
    public String id;
    public String name;
    public String version;
    //Fully qualified name of the Plugin main class
    public String mainClass;

    public PluginMetadata(String id, String name, String version, String mainClass) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.mainClass = mainClass;
    }
}
