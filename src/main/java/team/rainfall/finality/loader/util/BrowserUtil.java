package team.rainfall.finality.loader.util;

import team.rainfall.finality.FinalityLogger;

import java.awt.*;
import java.net.URI;

public class BrowserUtil {
    public static void openUrl(String url){
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e){
            FinalityLogger.warn("Failed to open browser");
        }
    }
}
