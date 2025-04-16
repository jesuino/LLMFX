package org.fxapps.llmfx.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.AlertsHelper;
import org.fxapps.llmfx.Events.NewChatEvent;
import org.fxapps.llmfx.Events.DeleteConversationEvent;
import org.fxapps.llmfx.Events.HistorySelectedEvent;
import org.fxapps.llmfx.Events.MCPServerSelectEvent;
import org.fxapps.llmfx.Events.RefreshModelsEvent;
import org.fxapps.llmfx.Events.SaveChatEvent;
import org.fxapps.llmfx.Events.SaveFormat;
import org.fxapps.llmfx.Events.SelectedModelEvent;
import org.fxapps.llmfx.Events.StopStreamingEvent;
import org.fxapps.llmfx.Events.ToolSelectEvent;
import org.fxapps.llmfx.Events.UserInputEvent;
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
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

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
    Event<NewChatEvent> clearChatEvent;

    @Inject
    Event<SaveChatEvent> saveChatEvent;

    @Inject
    Event<MCPServerSelectEvent> mcpServerSelectEvent;

    @Inject
    Event<ToolSelectEvent> toolSelectEvent;

    @Inject
    Event<StopStreamingEvent> stopStreamingEvent;

    @Inject
    Event<RefreshModelsEvent> refreshModelsEvent;

    @Inject
    Event<HistorySelectedEvent> historySelectedEvent;

    @Inject
    Event<DeleteConversationEvent> deleteConversationEvent;

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

    @FXML
    private Button btnTrashConversation;

    @FXML
    ListView<String> historyList;

    @FXML
    private VBox vbWelcomeMessage;

    private SimpleBooleanProperty holdChatProperty;

    private boolean autoScroll;

    private Tooltip mcpMenuTooltip;

    public void init() {
        this.mcpMenuTooltip = new Tooltip("Select MCP Servers");
        holdChatProperty = new SimpleBooleanProperty();
        txtInput.disableProperty().bind(holdChatProperty);
        btnNewChat.disableProperty().bind(holdChatProperty);
        historyList.disableProperty().bind(holdChatProperty);
        btnStop.disableProperty().bind(holdChatProperty.not());
        mcpMenu.setTooltip(mcpMenuTooltip);

        chatOutput.setOnScroll(e -> autoScroll = false);

        chatOutput.getEngine().loadContent(CHAT_PAGE);

        this.historyList.getSelectionModel().selectedIndexProperty().addListener((obs, old, n) -> {
            final var i = n.intValue();
            if (i != -1) {
                historySelectedEvent.fire(new HistorySelectedEvent(i));
            }
        });

        btnTrashConversation.disableProperty()
                .bind(historyList.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
    }

    @FXML
    public void onInputAction() {
        final var input = txtInput.getText();
        if (!input.isBlank()) {
            txtInput.setText("");
            onUserInputEvent.fire(new UserInputEvent(input));
        }
    }

    public void enableMCPMenu(boolean enable) {
        var text = enable ? "MCP Servers are enabled" : "MCP Servers are ignored because Tools are selected";
        mcpMenuTooltip.setText(text);
        mcpMenu.setDisable(!enable);
    }

    @FXML
    void trashConversation() {
        final var i = historyList.getSelectionModel().getSelectedIndex();
        if (i == -1) {
            return;
        }
        final var confirmDelete = alertsHelper.showWarningWithConfirmation("Delete conversation",
                "Are you sure you want to delete the conversation?");

        if (confirmDelete) {
            deleteConversationEvent.fire(new DeleteConversationEvent(i));
            historyList.getSelectionModel().clearSelection();
        }

    }

    public BooleanProperty holdChatProperty() {
        return holdChatProperty;
    }

    public void setHistoryItems(Collection<String> items) {
        historyList.getItems().clear();
        historyList.getItems().addAll(items);
        historyList.getSelectionModel().selectLast();

    }

    public void setAutoScroll(boolean isAutoScroll) {
        this.autoScroll = isAutoScroll;
    }

    public void fillModels(List<String> modelsNames) {
        cmbModels.setItems(FXCollections.observableList(modelsNames));
    }

    public void appendUserMessage(String userMessage) {
        runScriptToAppendMessage("<p>" + userMessage + "</p>", "user");
    }

    public void appendAssistantMessage(String assistantMessage) {
        var message = assistantMessage.replaceFirst("<think>",
                """
                            <h4 style=\"color: red !important\">Thinking</h4>
                            <i style=\"color: gray\">
                        """)
                .replaceFirst("</think>", "</i><h4 style=\"color: red !important\">end thinking</h4><hr/>")
                // TODO: find someway to copy to the clipboard
                .replaceAll("<code", "<code");

        runScriptToAppendMessage(message, "assistant");

    }

    public void setSelectedModel(String model) {
        cmbModels.getSelectionModel().select(model);
    }

    public void clearChatHistoy() {
        vbWelcomeMessage.setVisible(true);
        chatOutput.getEngine().executeScript("document.getElementById('chatContent').innerHTML = ''");
    }

    public String getChatHistoryHTML() {
        return (String) chatOutput.getEngine().executeScript("document.documentElement.outerHTML");

    }

    public void setMCPServers(Collection<String> mcpServers) {
        final var mcpMenus = mcpServers.stream().map(mcpServer -> {
            var menu = new CheckMenuItem(mcpServer);
            menu.setOnAction(e -> {
                mcpServerSelectEvent.fire(new MCPServerSelectEvent(mcpServer,
                        menu.isSelected()));
            });
            return menu;

        }).toList();
        mcpMenu.getItems().addAll(mcpMenus);
    }

    public void setTools(Map<String, List<String>> toolsCat) {
        var catMenus = toolsCat.entrySet()
                .stream()
                .map(e -> {
                    Menu mnCat = new Menu(e.getKey());
                    e.getValue().stream().map(tool -> {
                        var menu = new CheckMenuItem(tool);
                        menu.setOnAction(evt -> toolSelectEvent.fire(
                                new ToolSelectEvent(tool, menu.isSelected())));
                        return menu;

                    }).forEach(mnCat.getItems()::add);
                    return mnCat;
                }).toList();
        toolsMenu.getItems().addAll(catMenus);
    }

    @FXML
    void modelSelected(ActionEvent event) {
        selectedModelEvent.fire(new SelectedModelEvent(cmbModels.getSelectionModel().getSelectedItem()));
    }

    @FXML
    void onRefreshModels() {
        refreshModelsEvent.fire(new RefreshModelsEvent());
    }

    @FXML
    void newChat(ActionEvent event) {
        vbWelcomeMessage.setVisible(true);
        clearChatEvent.fire(new NewChatEvent());
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

    private void runScriptToAppendMessage(String message, String role) {
        vbWelcomeMessage.setVisible(false);
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
        if (autoScroll)
            chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }
}
