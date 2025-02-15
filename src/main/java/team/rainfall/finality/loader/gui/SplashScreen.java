package team.rainfall.finality.loader.gui;

import team.rainfall.finality.loader.util.Localization;

import javax.swing.*;
import java.util.Objects;

public class SplashScreen extends JFrame {
    public static SplashScreen splashScreen = null;
    protected SplashScreen() {
        super(Localization.bundle.getString("splash_title"));
        //使用工程内的resources/splash.png文件
        JLabel label = new JLabel(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/splash.png"))));
        this.add(label);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setSize(800, 400);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
    public static void create(){
        if(splashScreen == null){
            splashScreen = new SplashScreen();
        }
    }

    public static void destroy(){
        if(splashScreen != null)
            splashScreen.dispose();
            splashScreen = null;
    }
}
