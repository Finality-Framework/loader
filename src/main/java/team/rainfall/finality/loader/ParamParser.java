package team.rainfall.finality.loader;

import org.apache.commons.cli.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.Localization;

public class ParamParser {
    public boolean forceNoVDF = false;
    public boolean isReboot = false;
    public LaunchMode mode = LaunchMode.ONLY_LAUNCH;
    public String gameFilePath = null;
    public boolean disableSteamAPI = false;

    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("h", "help", false, "Show help message");
        OPTIONS.addOption("forceNoVDF", false, "Force not to use VDF");
        OPTIONS.addOption("ignore", false, "Ignore remaining arguments");
        OPTIONS.addOption("reboot", false, "Reboot mode");
        OPTIONS.addOption("debug", false, "Enable debug mode");
        OPTIONS.addOption("disableSteamAPI", false, "Disable Steam API");
        OPTIONS.addOption("gamePath", true, "Specify game file path");
        
        Option launchModeOption = Option.builder()
                .longOpt("launchMode")
                .hasArg(true)
                .argName("mode")
                .desc("Launch mode: install, only-launch, only-gen, launch-and-gen")
                .build();
        OPTIONS.addOption(launchModeOption);
    }

    public void parse(String[] args) {
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(OPTIONS, args);
            
            if (cmd.hasOption("help")) {
                printHelp();
                return;
            }
            
            if (cmd.hasOption("forceNoVDF")) {
                forceNoVDF = true;
            }
            
            if (cmd.hasOption("ignore")) {
                return;
            }
            
            if (cmd.hasOption("reboot")) {
                isReboot = true;
            }
            
            if (cmd.hasOption("debug")) {
                FinalityLogger.isDebug = true;
            }
            
            if (cmd.hasOption("disableSteamAPI")) {
                disableSteamAPI = true;
            }
            
            if (cmd.hasOption("gamePath")) {
                gameFilePath = cmd.getOptionValue("gamePath");
                if (gameFilePath == null || gameFilePath.trim().isEmpty()) {
                    FinalityLogger.warn("Invalid game path");
                    this.gameFilePath = FileManager.INSTANCE.findGameFile();
                }
            }
            
            if (cmd.hasOption("launchMode")) {
                String modeValue = cmd.getOptionValue("launchMode");
                if (modeValue == null) {
                    FinalityLogger.warn(Localization.bundle.getString("invalid_launch_mode"));
                } else {
                    modeValue = modeValue.toLowerCase();
                    switch (modeValue) {
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
            
        } catch (ParseException e) {
            FinalityLogger.warn("Failed to parse command line arguments: " + e.getMessage());
            printHelp();
        }
    }
    
    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("FinalityLoader", OPTIONS);
        Loader.safeExit();
    }
    
    public static Options getOptions() {
        return OPTIONS;
    }
}
