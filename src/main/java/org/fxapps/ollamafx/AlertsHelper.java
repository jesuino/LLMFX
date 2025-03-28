package org.fxapps.ollamafx;

import jakarta.enterprise.context.ApplicationScoped;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

@ApplicationScoped
public class AlertsHelper {

    public boolean showWarningWithConfirmation(String header, String message) {
        Alert warningDialog = new Alert(Alert.AlertType.CONFIRMATION);
        warningDialog.setTitle("WARNING");
        warningDialog.setHeaderText(header);
        warningDialog.setContentText(message);
        return warningDialog.showAndWait().map(t -> t == ButtonType.OK).orElse(false);
    }

}
