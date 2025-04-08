package org.fxapps.llmfx;

import jakarta.enterprise.context.ApplicationScoped;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

@ApplicationScoped
public class AlertsHelper {

    public boolean showWarningWithConfirmation(String header, String message) {
        var warningDialog = new Alert(Alert.AlertType.CONFIRMATION);
        warningDialog.setTitle("WARNING");
        warningDialog.setHeaderText(header);
        warningDialog.setContentText(message);
        return warningDialog.showAndWait().map(t -> t == ButtonType.OK).orElse(false);
    }

    public void showError(String title, String header, String message) {
        var errroDialog = new Alert(Alert.AlertType.ERROR);
        errroDialog.setTitle("WARNING");
        errroDialog.setHeaderText(header);
        errroDialog.setContentText(message);
        errroDialog.showAndWait();
    }

}
