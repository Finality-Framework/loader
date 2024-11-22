package team.rainfall.finality.loader;

import team.rainfall.finality.FinalityLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParamParser {
    public Manifest manifest = new Manifest();
    public launchMode mode = launchMode.ONLY_LAUNCH;

    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                FinalityLogger.isDebug = true;
            }
            if (args[i].equals("-manifest")) {
                try {
                    manifest = new Manifest(Files.newInputStream(Paths.get(args[i + 1])));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (args[i].equals("-launchMode")) {
                if(args.length <= i + 1){
                    FinalityLogger.warn("Invalid launch mode, defaulting to only-launch");
                    break;
                }
                switch (args[i + 1]) {
                    case "only-launch":
                        mode = launchMode.ONLY_LAUNCH;
                        break;
                    case "only-gen":
                        mode = launchMode.ONLY_GEN;
                        break;
                    case "launch-and-gen":
                        mode = launchMode.LAUNCH_AND_GEN;
                        break;
                    default:
                        FinalityLogger.warn("Invalid launch mode, defaulting to only-launch");
                        break;

                }
            }
        }
    }
}
