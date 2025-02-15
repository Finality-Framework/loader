package team.rainfall.finality.loader.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {
    public static ResourceBundle bundle;
    static{
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle("language", locale,new UTF8Control());
    }
    //编写一个方法，判断用户是否使用zh-CN
    public static boolean isChinese(){
        return bundle.getLocale().equals(Locale.CHINA);
    }

}
