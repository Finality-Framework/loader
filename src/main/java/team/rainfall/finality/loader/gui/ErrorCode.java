package team.rainfall.finality.loader.gui;

import team.rainfall.finality.loader.util.Localization;

import javax.swing.*;

/**
 * <p>The ErrorCode class is responsible for displaying error messages to the user.</p>
 * <p>It ensures that multiple error messages are not shown simultaneously.</p>
 * <p>The error messages are localized using the Localization class.</p>
 *
 * @author RedreamR
 */
public class ErrorCode {

    //Avoid show multiple error messages
    public static boolean showed = false;

    /**
     * <p>Displays an internal error message with the specified error code.</p>
     * <p>The error message is shown in a dialog box.</p>
     *
     * @param code the error code to display
     * @author RedreamR
     */
    public static void showInternalError(String code){
        if(!showed) {
            String msg = String.format(Localization.bundle.getString("internal_error"), code);
            JOptionPane.showMessageDialog(null, msg, Localization.bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
        showed = true;
    }

}
