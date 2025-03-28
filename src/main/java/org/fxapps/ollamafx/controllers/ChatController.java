package org.fxapps.ollamafx.controllers;

import java.io.IOException;
import java.util.List;

import org.fxapps.ollamafx.AlertsHelper;
import org.fxapps.ollamafx.events.ClearChatEvent;
import org.fxapps.ollamafx.events.SelectedModelEvent;
import org.fxapps.ollamafx.events.UserInputEvent;
import io.quarkiverse.fx.views.FxView;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

@FxView
@Dependent
public class ChatController {

    final String CHAT_PAGE = """
                <html>
                    <style>
                        .chat-container {
                            display: flex;
                            padding: 10px;
                            flex-direction: column;
                            align-items: flex-start;
                            gat: 10;

                        }
                        .left-message .right-message {

                        }
                        .left-message {
                            text-align: right;
                            background-color: lightblue;
                            padding: 10px;
                            border-radius: 20px;
                            color: black;
                            float: right;
                            margin-bottom: 15px;
                            margin-top: 15px;
                        }

                        .right-message {
                            text-align: left;
                            background-color: lightgray;
                            padding: 10px;
                            border-radius: 20px;
                            color: black;
                        }
                    </style>
                    <body>
                        <div id="chatContent" class="chat-container">
                        </div>
                    </body>
                </html>
            """;

    @Inject
    Event<UserInputEvent> onUserInputEvent;

    @Inject
    Event<SelectedModelEvent> selectedModelEvent;

    @Inject
    Event<ClearChatEvent> clearChatEvent;

    @Inject
    AlertsHelper alertsHelper;

    @FXML
    private WebView chatOutput;

    @FXML
    private ComboBox<String> cmbModels;

    @FXML
    private TextField txtInput;

    @FXML
    public void onInputAction() {
        final var input = txtInput.getText();
        if (!input.isBlank()) {
            txtInput.setText("");
            onUserInputEvent.fireAsync(new UserInputEvent(input));
        }

    }

    @FXML
    void modelSelected(ActionEvent event) {
        selectedModelEvent.fire(new SelectedModelEvent(cmbModels.getSelectionModel().getSelectedItem()));
    }

    @FXML
    void newChat(ActionEvent event) {
        var clearChat = alertsHelper.showWarningWithConfirmation("Start a new Chat?",
                "Make sure you saved your work or it will be lost!");

        if (clearChat) {
            clearChatEvent.fire(new ClearChatEvent());
        }
    }

    @FXML
    void saveAsHTML(ActionEvent event) {

    }

    @FXML
    void saveAsJSON(ActionEvent event) {

    }

    @FXML
    void saveAsText(ActionEvent event) {

    }

    public BooleanProperty chatDisableProperty() {
        return txtInput.disableProperty();
    }

    public void fillModels(List<String> modelsNames) {
        cmbModels.setItems(FXCollections.observableList(modelsNames));
    }

    public void initializeWebView() throws IOException {
        chatOutput.getEngine().loadContent(CHAT_PAGE);
    }

    private void adjustScroll() {
        chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public void appendUserMessage(String userMessage) {
        runScriptToAppendMessage(userMessage, "left");
    }

    public void appendAssistantMessage(String assistantMessage) {
        runScriptToAppendMessage(assistantMessage, "right");

    }

    public void updateLastAssistantMessage(String content) {
        final var lastAssistantMessageSelector = "document.querySelector('.chat-container .right-message:last-child')";
        final var engine = chatOutput.getEngine();

        Object currentWorkingContent = engine.executeScript(lastAssistantMessageSelector + ".innerHTML");
        // avoids blinks due exceptions
        try {
            engine.executeScript(lastAssistantMessageSelector + ".innerHTML = `" + content + "`");
        } catch (Exception e) {
            engine.executeScript(lastAssistantMessageSelector + ".innerHTML = `" + currentWorkingContent + "`");
        }
    }

    private void runScriptToAppendMessage(String message, String messagePos) {
        var addUserMessageScriptTemplate = """
                        var pos = '%s';
                        var messageContent = document.createElement('div');
                        messageContent.setAttribute("class", `${pos}-message`);
                        messageContent.innerHTML = `%s`;
                        document.getElementById('chatContent').appendChild(messageContent);
                """;

        var script = String.format(addUserMessageScriptTemplate, messagePos, message);
        chatOutput.getEngine().executeScript(script);
        adjustScroll();

    }

    public void setSelectedModel(String ollamaModel) {
        cmbModels.getSelectionModel().select(ollamaModel);
    }

    public void clearChatHistoy() {
        chatOutput.getEngine().executeScript("document.getElementById('chatContent').innerHTML = ''");
    }
}
