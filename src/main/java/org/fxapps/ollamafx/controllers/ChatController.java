package org.fxapps.ollamafx.controllers;

import java.io.IOException;
import java.util.List;

import org.fxapps.ollamafx.AlertsHelper;
import org.fxapps.ollamafx.events.ClearChatEvent;
import org.fxapps.ollamafx.events.SaveChatEvent;
import org.fxapps.ollamafx.events.SelectedModelEvent;
import org.fxapps.ollamafx.events.UserInputEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDivElement;
import org.w3c.dom.html.HTMLElement;
import org.fxapps.ollamafx.events.SaveChatEvent.Format;

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
                            gap: 10;

                        }
                        .user-message {
                            background-color: lightblue;
                            border-radius: 20px;
                            color: black;
                            margin-top: 15px;
                            padding: 5px;
                        }

                        .assistant-message {
                            padding: 5px;
                            background-color: lightgray;
                            border-radius: 20px;
                            color: black;
                            margin-top: 15px;
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
    Event<SaveChatEvent> saveChatEvent;

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
        saveChatEvent.fire(new SaveChatEvent(Format.HTML));
    }

    @FXML
    void saveAsJSON(ActionEvent event) {
        saveChatEvent.fire(new SaveChatEvent(Format.JSON));
    }

    @FXML
    void saveAsText(ActionEvent event) {
        saveChatEvent.fire(new SaveChatEvent(Format.TEXT));

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
        runScriptToAppendMessage("<p>" + userMessage + "</p>", "user");
    }

    public void appendAssistantMessage(String assistantMessage) {
        runScriptToAppendMessage(assistantMessage, "assistant");

    }

    private void runScriptToAppendMessage(String message, String role) {
        // workaround because I don't have an innerHTML method in Java API!
        var script = """
                var messageContent = document.createElement('div');
                var tmp = document.querySelector('#tmp');
                messageContent.setAttribute("class", '%s-message');                
                messageContent.innerHTML = tmp.textContent;
                document.querySelector('#chatContent').appendChild(messageContent);
                tmp.remove();
                """.formatted(role);
        
        var el = chatOutput.getEngine().getDocument().createElement("div");
        el.setTextContent(message);
        el.setAttribute("id", "tmp");
        el.setAttribute("hidden", "true");
        var chatRoot = (HTMLElement) chatOutput.getEngine().getDocument().getElementById("chatContent");
        chatRoot.appendChild(el);
        chatOutput.getEngine().executeScript(script);
        adjustScroll();

    }

    public void setSelectedModel(String ollamaModel) {
        cmbModels.getSelectionModel().select(ollamaModel);
    }

    public void clearChatHistoy() {
        chatOutput.getEngine().executeScript("document.getElementById('chatContent').innerHTML = ''");
    }

    public String getChatHistoryHTML() {
        return (String) chatOutput.getEngine().executeScript("document.documentElement.outerHTML");

    }
}
