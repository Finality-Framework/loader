package team.rainfall.finality.loader.game;

import team.rainfall.finality.FinalityLogger;

import java.io.*;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class ProductDetector {
    private final Score score = new Score();
    private final JarFile jarFile;
    private final File steamID;
    public ProductDetector(JarFile jarFile,File steamID){
        this.jarFile = jarFile;
        this.steamID = steamID;
    }
    public Product detect(){
        if(jarFile.getEntry("age") != null) {
            score.AoH2++;
        }
        if(jarFile.getEntry("aoc") != null){
           score.AoH3++;
        }
        if(score.AoH2 == 1 && score.AoH3 == 1){
            score.AoH2DE++;
        }
        try {
            String value = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if(value.startsWith("aoc")) {
                score.AoH3++;
                score.AoH2DE++;
            }
            if(value.startsWith("age")) score.AoH2++;
        } catch (IOException ignored) {

        }

        switch (detectBySteamAppID(steamID)) {
            case AoH2:
                score.AoH2++;
                break;
            case AoH3:
                score.AoH3++;
                break;
            case AoH2DE:
                score.AoH2DE++;
                break;
            default:
                break;
        }
        if(score.AoH2DE > score.AoH3 && score.AoH2DE > score.AoH2) return Product.AoH2DE;
        if(score.AoH2 > score.AoH3) return Product.AoH2;
        if(score.AoH3 > score.AoH2) return Product.AoH3;
        return null;
    }
    public Product detectBySteamAppID(File file){
        if(!file.exists() || file.isDirectory()){
            return null;
        }
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath()))){
            String appIDStr = dis.readLine().trim();
            appIDStr = appIDStr.replaceAll("\n","");
            FinalityLogger.info("Detected Steam APP ID:"+appIDStr);
            switch (appIDStr){
                case "2772750":
                    return Product.AoH3;
                case "603850":
                    return Product.AoH2;
                case "3381680":
                    return Product.AoH2DE;
                default:
                    return null;
            }


        } catch (IOException e) {
            FinalityLogger.error("Error while detecting by Steam App ID",e);
            return null;
        }
    }
}
class Score{
    int AoH2 = 0;
    int AoH3 = 0;
    int AoH2DE = 0;
}