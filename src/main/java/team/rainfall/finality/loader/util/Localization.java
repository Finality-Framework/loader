package team.rainfall.finality.loader.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Utility class for handling localization and resource bundles.
 * <p>This class provides methods to determine the locale and load resource bundles accordingly.
 * <p>Note: This class uses the default locale to load the resource bundle.
 *
 * @author RedreamR
 */
public class Localization {

    public static ResourceBundle bundle;
    static{
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle("language", locale,new UTF8Control());
    }

    /**
     * <p>Checks if the current locale is Chinese.
     *
     * @return true if the current locale is Chinese, false otherwise
     * @author RedreamR
     */
    public static boolean isChinese(){
        return bundle.getLocale().equals(Locale.CHINA);
    }

}
