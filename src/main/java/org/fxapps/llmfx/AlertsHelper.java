package org.fxapps.llmfx;

import java.io.File;
import java.util.Optional;

import io.quarkiverse.fx.FxPostStartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Singleton
public class AlertsHelper {

    Stage ownerWindow;

    void onPostStartup(@Observes final FxPostStartupEvent event) throws Exception {
        this.ownerWindow = event.getPrimaryStage();

    }

    public boolean showWarningWithConfirmation(String header, String message) {
        var warningDialog = new Alert(Alert.AlertType.CONFIRMATION);
        warningDialog.setTitle("WARNING");
        warningDialog.setHeaderText(header);
        warningDialog.setContentText(message);
        warningDialog.initOwner(ownerWindow);
        return warningDialog.showAndWait().map(t -> t == ButtonType.OK).orElse(false);
    }

    public void showError(String title, String header, String message) {
        var errroDialog = new Alert(Alert.AlertType.ERROR);
        errroDialog.setTitle("WARNING");
        errroDialog.setHeaderText(header);
        errroDialog.setContentText(message);
        errroDialog.initOwner(ownerWindow);
        errroDialog.showAndWait();
    }

    public Optional<File> showFileChooser(String title, String fileExtension) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return Optional.ofNullable(fileChooser.showSaveDialog(ownerWindow))
                .map(file -> file.getName().endsWith(fileExtension)
                        ? file
                        : new File(file.getAbsolutePath() + "." + fileExtension));
    }

}
