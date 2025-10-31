package org.fxapps.llmfx.windows;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.fxapps.llmfx.Events.ChatModelErrorEvent;
import org.fxapps.llmfx.Events.ChatModelRequestEvent;
import org.fxapps.llmfx.Events.ChatModelResponseEvent;
import org.fxapps.llmfx.services.HistoryStorage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

@Singleton
public class ViewLogsDialog extends Dialog<Boolean> {

    public static final ButtonType CLEAR_BUTTON_TYPE = new ButtonType("Clear", ButtonData.OTHER);
    TextArea txtLogs;
    @Inject
    HistoryStorage historyStorage;

    @PostConstruct
    void init() {
        txtLogs = new TextArea();
        txtLogs.setEditable(false);
        var body = new StackPane(txtLogs);
        VBox.setVgrow(txtLogs, Priority.ALWAYS);
        getDialogPane().setPrefSize(600, 400);
        txtLogs.setPrefSize(600, 400);
        body.setPrefSize(600, 400);
        initModality(Modality.NONE);
        setTitle("App Logs");
        setResizable(true);
        getDialogPane().getButtonTypes().addAll(CLEAR_BUTTON_TYPE, ButtonType.CLOSE);
        getDialogPane().setContent(txtLogs);

        var btnClear = (Button) getDialogPane().lookupButton(CLEAR_BUTTON_TYPE);
        btnClear.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    txtLogs.clear();
                    event.consume();
                });
    }

    public void onChatModelRequest(@Observes ChatModelRequestEvent chatModelRequestEvent) {
        var request = chatModelRequestEvent.requestContext().chatRequest();
        request.messages();
        var entry = LocalDateTime.now() + "\n";
        entry += " [REQUEST]\n";
        entry += "Model: " + request.modelName() + "\n";
        entry += "Messages: \n";
        entry += historyStorage.getConversation()
                .messages()
                .stream()
                .filter(m -> !m.text().isBlank())
                .map(m -> "\t" + m.role() + ": " + m.text())
                .collect(Collectors.joining("\n")) + "\n";
        entry += "Tools: \n";
        entry += request.toolSpecifications()
                .stream()
                .map(ts -> "\t" + ts.name() + ": " + ts.description())
                .collect(Collectors.joining("\n")) + "\n";
        entry += "[/REQUEST]\n";
        txtLogs.appendText(entry);
    }

    public void onChatResponseRequest(@Observes ChatModelResponseEvent chatModelResponsetEvent) {
        var response = chatModelResponsetEvent.responseContext().chatResponse();
        var entry = LocalDateTime.now() + "\n";
        entry += "[RESPONSE]\n";
        entry += "Message: " + response.aiMessage().text() + "\n";
        entry += "Executed Tools: \n";
        entry += response.aiMessage()
                .toolExecutionRequests()
                .stream()
                .map(tl -> "\t" + tl.name() + "[" + tl.arguments() + "]\n")
                .collect(Collectors.joining()) + "\n";
        entry += "[/RESPONSE]\n";
        txtLogs.appendText(entry);
    }

    public void onChatErrorRequest(@Observes ChatModelErrorEvent chatModelErrorEvent) {
        var entry = LocalDateTime.now() + "\n";
        entry += "[ERROR]\n";
        entry += chatModelErrorEvent.chatModelError().error().getMessage();
        entry += "[/ERROR]\n";
        txtLogs.appendText(entry);
    }
}
