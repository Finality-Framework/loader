package team.rainfall.finality.loader;

import org.apache.commons.cli.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.game.Product;
import team.rainfall.finality.loader.util.Localization;

public class ParamParser {
    public boolean forceNoVDF = false;
    public boolean isReboot = false;
    public LaunchMode mode = LaunchMode.ONLY_LAUNCH;
    public String gameFilePath = null;
    public boolean disableSteamAPI = false;
    public boolean testCrash = false;
    public boolean headless = false;
    public boolean noUpdateCheck = false;
    public String composeConfigPath = null;
    public ComposeTarget composeTarget = null;
    public String patchConfigPath = null;
    public Product product = null;
    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("h", "help", false, "Show help message");
        OPTIONS.addOption(Option.builder().longOpt("force-no-vdf").desc("Force not to use VDF").build());
        OPTIONS.addOption(Option.builder().longOpt("ignore").desc("Ignore remaining arguments").build());
        OPTIONS.addOption("r", "reboot", false, "Reboot mode");
        OPTIONS.addOption("d", "debug", false, "Enable debug mode");
        OPTIONS.addOption(Option.builder().longOpt("disable-steam-api").desc("Disable Steam API").build());
        OPTIONS.addOption(Option.builder().longOpt("test-crash").desc("Trigger a test crash (Error Code: Etude - 99)").build());
        OPTIONS.addOption(Option.builder().longOpt("headless").desc("Disable all GUI interfaces").build());
        OPTIONS.addOption(Option.builder().longOpt("no-update-check").desc("Do not check for updates").build());
        OPTIONS.addOption("g", "game-path", true, "Specify game file path");
        OPTIONS.addOption("p", "product", true, "Specify product type. Valid values: aoh2, aoh3, aoh2de");
        OPTIONS.addOption(Option.builder("c")
                .longOpt("compose-config")
                .hasArg()
                .argName("path")
                .desc("Path to compose.json config file")
                .build());
        OPTIONS.addOption(Option.builder("t")
                .longOpt("compose-target")
                .hasArg()
                .argName("target")
                .desc("Compose target: android, desktop")
                .build());
        OPTIONS.addOption(Option.builder()
                .longOpt("patch-config")
                .hasArg()
                .argName("path")
                .desc("Path to patch.json config file")
                .build());
        OPTIONS.addOption(Option.builder("m")
                .longOpt("launch-mode")
                .hasArg()
                .argName("mode")
                .desc("Launch mode: install, only-launch, only-gen, launch-and-gen, compose, patch")
                .build());
    }

    public void parse(String[] args) {
        normalizeLegacyArgs(args);
        CommandLineParser parser = new DefaultParser();
        
        try {
            CommandLine cmd = parser.parse(OPTIONS, args);
            
            if (cmd.hasOption("help")) {
                printHelp();
                return;
            }
            
            if (cmd.hasOption("force-no-vdf")) {
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
            
            if (cmd.hasOption("disable-steam-api")) {
                disableSteamAPI = true;
            }

            if (cmd.hasOption("test-crash")) {
                testCrash = true;
            }

            if (cmd.hasOption("headless")) {
                headless = true;
            }

            if (cmd.hasOption("no-update-check")) {
                noUpdateCheck = true;
            }

            if (cmd.hasOption("compose-config")) {
                composeConfigPath = cmd.getOptionValue("compose-config");
            }

            if (cmd.hasOption("compose-target")) {
                String targetValue = cmd.getOptionValue("compose-target");
                if (targetValue == null) {
                    FinalityLogger.warn(Localization.bundle.getString("invalid_compose_target"));
                } else {
                    targetValue = targetValue.toLowerCase();
                    switch (targetValue) {
                        case "android":
                            composeTarget = ComposeTarget.ANDROID;
                            break;
                        case "desktop":
                            composeTarget = ComposeTarget.DESKTOP;
                            break;
                        default:
                            FinalityLogger.warn(Localization.bundle.getString("invalid_compose_target"));
                            break;
                    }
                }
            }

            if (cmd.hasOption("patch-config")) {
                patchConfigPath = cmd.getOptionValue("patch-config");
            }
            
            if (cmd.hasOption("game-path")) {
                gameFilePath = cmd.getOptionValue("game-path");
                if (gameFilePath == null || gameFilePath.trim().isEmpty()) {
                    FinalityLogger.warn("Invalid game path");
                    this.gameFilePath = FileManager.INSTANCE.findGameFile();
                }
            }
            if (cmd.hasOption("product")) {
                String productValue = cmd.getOptionValue("product");
                if (productValue == null) {
                    FinalityLogger.warn(Localization.bundle.getString("invalid_product"));
                } else {
                    productValue = productValue.toLowerCase();
                    switch (productValue) {
                        case "aoh2":
                            product = Product.AoH2;
                            break;
                        case "aoh3":
                            product = Product.AoH3;
                            break;
                        case "aoh2de":
                            product = Product.AoH2DE;
                        default:
                            FinalityLogger.warn(Localization.bundle.getString("invalid_product"));
                            break;
                    }
                }
            }
            
            if (cmd.hasOption("launch-mode")) {
                String modeValue = cmd.getOptionValue("launch-mode");
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
                        case "compose":
                            mode = LaunchMode.COMPOSE;
                            break;
                        case "patch":
                            mode = LaunchMode.PATCH;
                            headless = true;
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
        formatter.printHelp("FinalityLoader", null, OPTIONS, "Legacy option spellings (e.g. --forceNoVDF, -gamePath, -launchMode) are still accepted.", true);
        Loader.safeExit();
    }

    private static void normalizeLegacyArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-forceNoVDF":
                case "--forceNoVDF":
                    args[i] = "--force-no-vdf";
                    break;
                case "-disableSteamAPI":
                case "--disableSteamAPI":
                    args[i] = "--disable-steam-api";
                    break;
                case "-testCrash":
                case "--testCrash":
                    args[i] = "--test-crash";
                    break;
                case "-gamePath":
                case "--gamePath":
                    args[i] = "--game-path";
                    break;
                case "-launchMode":
                case "--launchMode":
                    args[i] = "--launch-mode";
                    break;
                case "-debug":
                    args[i] = "--debug";
                    break;
                case "-ignore":
                    args[i] = "--ignore";
                    break;
                case "-reboot":
                    args[i] = "--reboot";
                    break;
                default:
                    break;
            }
        }
    }
    
    public static Options getOptions() {
        return OPTIONS;
    }
}
