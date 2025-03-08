package team.rainfall.finality.loader.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {
    public static ResourceBundle bundle;
    static{
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle("language", locale,new UTF8Control());
    }

    public static boolean isChinese(){
        return bundle.getLocale().equals(Locale.CHINA);
    }

}
