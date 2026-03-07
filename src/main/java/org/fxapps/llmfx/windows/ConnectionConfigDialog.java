package org.fxapps.llmfx.windows;

import org.fxapps.llmfx.config.RuntimeLLMConfig;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Singleton
public class ConnectionConfigDialog extends Dialog<ConnectionConfigDialog.ConnectionConfig> {

    private TextField urlField = new TextField();
    private TextField modelField = new TextField();
    private PasswordField keyField = new PasswordField();

    @Inject
    RuntimeLLMConfig runtimeLLMConfig;

    public record ConnectionConfig(String url, String key, String model) {
    }

    @PostConstruct
    public void setup() {
        setTitle("LLM Connection Configuration");
        setHeaderText("Please configure the LLM Server connection");

        this.urlField = new TextField();
        this.modelField = new TextField();
        this.keyField = new PasswordField();

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("URL:"), 0, 0);
        grid.add(urlField, 1, 0);
        grid.add(new Label("Key:"), 0, 1);
        grid.add(keyField, 1, 1);
        grid.add(new Label("Default model:"), 0, 2);
        grid.add(modelField, 1, 2);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new ConnectionConfig(urlField.getText(), keyField.getText(), modelField.getText());
            }
            return null;
        });

        setOnShowing(e -> {
            urlField.setText(runtimeLLMConfig.url());
            keyField.setText(runtimeLLMConfig.key().orElse(""));
            modelField.setText(runtimeLLMConfig.model().orElse(""));
        });
    }

}
