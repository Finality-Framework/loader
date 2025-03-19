package team.rainfall.finality.loader.util;

import team.rainfall.finality.FinalityLogger;

import java.awt.*;
import java.net.URI;

/**
 * Utility class for opening URLs in the default web browser.
 * This class provides a method to open a given URL in the system's default web browser.
 * Note: This class uses {@link Desktop#getDesktop()} to open the URL, which may not be supported on all platforms.
 * If an error occurs while attempting to open the URL, a warning message is logged using {@link FinalityLogger}.
 *
 * @see Desktop
 * @see URI
 * @see FinalityLogger
 * @author RedreamR
 */
public class BrowserUtil {

    /**
     * Opens the specified URL in the system's default web browser.
     * This method attempts to open the given URL in the default web browser. If the operation fails,
     * a warning message is logged.
     *
     * @param url the URL to be opened
     * @throws IllegalArgumentException if the URL is null or empty
     * @author RedreamR
     */
    public static void openUrl(String url){
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e){
            FinalityLogger.warn("Failed to open browser");
        }
    }
}
