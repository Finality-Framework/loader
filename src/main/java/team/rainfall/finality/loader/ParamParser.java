package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.Localization;

/**
 * <p>The ParamParser class is responsible for parsing command line arguments
 * <p>and setting the appropriate parameters for the application.
 * <p>It handles various flags and options that can be passed to the application.
 *
 * @author RedreamR
 */
public class ParamParser {

    public boolean forceNoVDF = false;
    public boolean isReboot = false;
    public LaunchMode mode = LaunchMode.ONLY_LAUNCH;
    public String gameFilePath = null;
    public boolean disableSteamAPI = false;

    /**
     * <p>Parses the command line arguments and sets the corresponding parameters.
     *
     * @param args the command line arguments
     * @author RedreamR
     */
    public void parse(String[] args) {
        gameFilePath = FileManager.INSTANCE.findGameFile();
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-forceNoVDF")){
                forceNoVDF = true;
            }
            if(args[i].equals("-ignore")){
                break;
            }
            if(args[i].equals("-reboot")){
                isReboot = true;
            }
            if (args[i].equals("-debug")) {
                FinalityLogger.isDebug = true;
            }
            if(args[i].equals("-disableSteamAPI")){
                disableSteamAPI = true;
            }
            if (args[i].equals("-gamePath")) {
                if(args.length <= i + 1){
                    FinalityLogger.warn("Invalid game path");
                    gameFilePath = FileManager.INSTANCE.findGameFile();
                    break;
                }
                gameFilePath = args[i+1];
            }
            if (args[i].equals("-launchMode")) {
                if(args.length <= i + 1){
                    FinalityLogger.warn(Localization.bundle.getString("invalid_launch_mode"));
                    break;
                }
                switch (args[i + 1]) {
                    case "install":
                        mode = LaunchMode.INSTALL;
                        break;
                    case "only-launch":
                        mode = LaunchMode.ONLY_LAUNCH;
                        break;
                    case "only-gen":
                        mode = LaunchMode.ONLY_GEN;
                        break;
                    case "launch-and-gen":
                        mode = LaunchMode.LAUNCH_AND_GEN;
                        break;
                    default:
                        FinalityLogger.warn(Localization.bundle.getString("invalid_launch_mode"));
                        break;

                }
            }
        }
    }

}
