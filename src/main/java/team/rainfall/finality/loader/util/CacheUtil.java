package team.rainfall.finality.loader.util;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for cache operations.
 * This class provides methods to handle cache-related operations such as generating temporary cache files.
 * Note: This class uses {@link File#createTempFile(String, String)} to create temporary files.
 *
 * @see File
 * @author RedreamR
 */
public class CacheUtil {

    /**
     * Generates a temporary cache file with the specified name.
     * This method creates a temporary file with the prefix "Finality-Loader" and the specified name as the suffix.
     *
     * @param name the suffix for the temporary file name
     * @return the generated temporary file
     * @throws RuntimeException if an I/O error occurs
     * @author RedreamR
     */
    public static File generateCache(String name){
        try {
            return File.createTempFile("Finality-Loader",name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
