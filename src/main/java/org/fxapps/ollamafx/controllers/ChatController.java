package org.fxapps.ollamafx.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.fxapps.ollamafx.AlertsHelper;
import org.fxapps.ollamafx.Events.ClearChatEvent;
import org.fxapps.ollamafx.Events.MCPServerSelectEvent;
import org.fxapps.ollamafx.Events.SaveChatEvent;
import org.fxapps.ollamafx.Events.SaveFormat;
import org.fxapps.ollamafx.Events.SelectedModelEvent;
import org.fxapps.ollamafx.Events.StopStreamingEvent;
import org.fxapps.ollamafx.Events.ToolSelectEvent;
import org.fxapps.ollamafx.Events.UserInputEvent;
import org.w3c.dom.html.HTMLElement;

import io.quarkiverse.fx.views.FxView;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

@FxView
@Singleton
public class ChatController {

    final String CHAT_PAGE = """
                <html>
                    <style>
                        .chat-container {
                            padding: 10px;
                            flex-grow: 1;
                        }
                        .chat-container > p {
                            border-radius: 20px;
                            color: black;
                            margin: 5px;
                            padding: 5px;
                        }
                        .user-message {
                            background-color: lightblue;
                        }
                        .assistant-message {
                            background-color: lightgray;
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
    Event<MCPServerSelectEvent> mcpServerSelectEvent;

    @Inject
    Event<ToolSelectEvent> toolSelectEvent;

    @Inject
    Event<StopStreamingEvent> stopStreamingEvent;

    @Inject
    AlertsHelper alertsHelper;

    @FXML
    private WebView chatOutput;

    @FXML
    private ComboBox<String> cmbModels;

    @FXML
    private MenuButton mcpMenu;

    @FXML
    private MenuButton toolsMenu;

    @FXML
    private TextField txtInput;

    @FXML
    private Button btnNewChat;

    @FXML
    private Button btnStop;

    private SimpleBooleanProperty holdChatProperty;

    private Tooltip mcpMenuTooltip;

    public void init() {
        this.mcpMenuTooltip = new Tooltip("Select MCP Servers");
        mcpMenuTooltip.setHideDelay(Duration.seconds(2));
        holdChatProperty = new SimpleBooleanProperty();
        txtInput.disableProperty().bind(holdChatProperty);
        btnNewChat.disableProperty().bind(holdChatProperty);
        btnStop.disableProperty().bind(holdChatProperty.not());
        mcpMenu.setTooltip(mcpMenuTooltip);
    }

    @FXML
    public void onInputAction() {
        final var input = txtInput.getText();
        if (!input.isBlank()) {
            txtInput.setText("");
            onUserInputEvent.fireAsync(new UserInputEvent(input));
        }
    }

    public void enableMCPMenu(boolean enable) {
        var text = enable ? "MCP Servers are enabled" : "MCP Servers are ignored because Tools are selected";
        mcpMenuTooltip.setText(text);
        mcpMenu.setDisable(!enable);
    }

    public BooleanProperty holdChatProperty() {
        return holdChatProperty;
    }

    public void fillModels(List<String> modelsNames) {
        cmbModels.setItems(FXCollections.observableList(modelsNames));
    }

    public void initializeWebView() throws IOException {
        chatOutput.getEngine().loadContent(CHAT_PAGE);
    }

    public void appendUserMessage(String userMessage) {
        runScriptToAppendMessage("<p>" + userMessage + "</p>", "user");
    }

    public void appendAssistantMessage(String assistantMessage) {
        runScriptToAppendMessage(assistantMessage, "assistant");

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

    public void setMCPServers(Collection<String> mcpServers) {
        final var mcpMenus = mcpServers.stream().map(mcpServer -> {
            var menu = new CheckMenuItem(mcpServer);
            menu.setOnAction(e -> {
                mcpServerSelectEvent.fireAsync(new MCPServerSelectEvent(mcpServer,
                        menu.isSelected()));
            });
            return menu;

        }).toList();
        mcpMenu.getItems().addAll(mcpMenus);
    }

    public void setTools(Collection<String> tools) {
        final var toolsMenus = tools.stream().map(tool -> {
            var menu = new CheckMenuItem(tool);
            menu.setOnAction(e -> {
                toolSelectEvent.fireAsync(new ToolSelectEvent(tool,
                        menu.isSelected()));
            });
            return menu;

        }).toList();
        toolsMenu.getItems().addAll(toolsMenus);
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
        saveChatEvent.fire(new SaveChatEvent(SaveFormat.HTML));
    }

    @FXML
    void saveAsJSON(ActionEvent event) {
        saveChatEvent.fire(new SaveChatEvent(SaveFormat.JSON));
    }

    @FXML
    void saveAsText(ActionEvent event) {
        saveChatEvent.fire(new SaveChatEvent(SaveFormat.TEXT));

    }

    @FXML
    void stopStreaming() {
        stopStreamingEvent.fire(new StopStreamingEvent());

    }

    private void adjustScroll() {
        chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    private void runScriptToAppendMessage(String message, String role) {
        // workaround because I don't have an innerHTML method in Java API!
        var script = """
                var messageContent = document.createElement('p');
                var tmp = document.querySelector('#tmp');
                messageContent.setAttribute("class", '%s-message');
                messageContent.innerHTML = tmp.textContent;
                document.querySelector('#chatContent').appendChild(messageContent);
                tmp.remove();
                """.formatted(role);

        var el = chatOutput.getEngine().getDocument().createElement("p");
        el.setTextContent(message);
        el.setAttribute("id", "tmp");
        el.setAttribute("hidden", "true");
        var chatRoot = (HTMLElement) chatOutput.getEngine().getDocument().getElementById("chatContent");
        chatRoot.appendChild(el);
        chatOutput.getEngine().executeScript(script);
        adjustScroll();

    }
}
