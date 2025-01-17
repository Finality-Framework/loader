package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ParamParser {
    public ArrayList<String> modPaths = new ArrayList<>();
    public LaunchMode mode = LaunchMode.ONLY_LAUNCH;
    public String gameFilePath = FileManager.INSTANCE.findGameFile();
    public boolean disableSteamAPI = false;
    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
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
                    FinalityLogger.warn("Invalid launch mode, defaulting to only-launch");
                    break;
                }
                switch (args[i + 1]) {
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
                        FinalityLogger.warn("Invalid launch mode, defaulting to only-launch");
                        break;

                }
            }
        }
    }
}
