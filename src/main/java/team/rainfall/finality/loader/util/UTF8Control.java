package team.rainfall.finality.loader.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * <p>Custom ResourceBundle.Control implementation to read properties files as UTF-8.
 * <p>This class overrides the newBundle method to ensure properties files are read using UTF-8 encoding.
 * <p>Note: This class is used to handle localization with UTF-8 encoded properties files.
 *
 * <@author RedreamR
 */
public class UTF8Control extends ResourceBundle.Control {

    /**
     * <p>Creates a new ResourceBundle using UTF-8 encoding.
     *
     * @param baseName the base name of the resource bundle
     * @param locale the locale for which the resource bundle should be instantiated
     * @param format the resource bundle format to be loaded
     * @param loader the class loader to use to load the resource bundle
     * @param reload the flag to indicate bundle reloading
     * @return the resource bundle instance
     * @throws IOException if an I/O error occurs
     * @author RedreamR
     */
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        // The below is a copy of the default implementation.
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try {
                // Only this line is changed to make it to read properties files as UTF-8.
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }

}
