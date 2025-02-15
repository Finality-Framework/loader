package team.rainfall.finality.loader.util;

import java.io.File;
import java.io.IOException;

public class CacheUtil {
    public static File generateCache(String name){
        try {
            return File.createTempFile("Finality-Loader",name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
