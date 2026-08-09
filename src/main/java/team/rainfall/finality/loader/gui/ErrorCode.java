package team.rainfall.finality.loader.gui;

import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.Loader;
import team.rainfall.finality.loader.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ErrorCode {
    //Avoid show multiple error messages
    public static boolean showed = false;

    public static void showInternalError(String code){
        if(!showed) {
            showed = true;
            if(Loader.isNoGui()){
                FinalityLogger.error("Internal error: " + code);
                return;
            }
            String rawFormat = Localization.bundle.getString("internal_error");
            String htmlMsg = "<html><body style='width:450px'>"
                    + rawFormat.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                    .replace("%s", "<span style='color:#696969; font-weight:bold; font-size:13px;'>" + code + "</span>")
                    + "</body></html>";
            showDialog(htmlMsg);
        }
    }

    public static void showError(String message){
        if(!showed) {
            showed = true;
            if(Loader.isNoGui()){
                FinalityLogger.error(message);
                return;
            }
            String htmlMsg = "<html><body style='width:450px'>"
                    + message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                    + "</body></html>";
            showDialog(htmlMsg);
        }
    }

    private static void showDialog(String htmlMessage) {
        List<String> recentLogs = FinalityLogger.getRecentLogs(30);
        StringBuilder logText = new StringBuilder();
        for (String line : recentLogs) {
            logText.append(line).append("\n");
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel msgLabel = new JLabel(htmlMessage);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(msgLabel, BorderLayout.NORTH);

        JTextArea logArea = new JTextArea(logText.toString());
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(650, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Recent Logs"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, panel, Localization.bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
    }
}
