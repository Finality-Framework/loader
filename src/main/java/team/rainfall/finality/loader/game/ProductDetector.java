package team.rainfall.finality.loader.game;

import team.rainfall.finality.FinalityLogger;

import java.io.*;
import java.nio.file.Files;
import java.util.jar.JarFile;

public class ProductDetector {
    public static Product detect(JarFile jarFile) {
        if(jarFile.getEntry("age") != null) {
            return Product.AoH2;
        }
        return Product.AoH3;
    }
    public static Product detectBySteamAppID(File file){
        if(!file.exists() || file.isDirectory()){
            return null;
        }
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath()))){
            String appIDStr = dis.readUTF().trim();
            FinalityLogger.info("Detected Steam APP ID:"+appIDStr);
            switch (appIDStr){
                case "2772750":
                    return Product.AoH3;
                case "603850":
                    return Product.AoH2;
                case "3381680":
                    FinalityLogger.error("AoH2DE support is not implemented yet!");
                    return Product.AoH2DE;
                default:
                    return null;
            }


        } catch (IOException e) {
            return null;
        }
    }
}
