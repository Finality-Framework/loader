package team.rainfall.finality.loader.gui;

import team.rainfall.finality.loader.util.Localization;

import javax.swing.*;

public class ErrorCode {
    public static void showInternalError(String code){
        String msg = String.format(Localization.bundle.getString("internal_error"), code);
        JOptionPane.showMessageDialog(null,msg,Localization.bundle.getString("error"),JOptionPane.ERROR_MESSAGE);
    }
}
