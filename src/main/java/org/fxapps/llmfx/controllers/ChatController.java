package org.fxapps.llmfx.controllers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fxapps.llmfx.AlertsHelper;
import org.fxapps.llmfx.Events.ClearDrawingEvent;
import org.fxapps.llmfx.Events.ClearReportEvent;
import org.fxapps.llmfx.Events.DeleteConversationEvent;
import org.fxapps.llmfx.Events.HistorySelectedEvent;
import org.fxapps.llmfx.Events.NewChatEvent;
import org.fxapps.llmfx.Events.NewDrawingNodeEvent;
import org.fxapps.llmfx.Events.NewReportingNodeEvent;
import org.fxapps.llmfx.Events.RefreshModelsEvent;
import org.fxapps.llmfx.Events.SaveChatEvent;
import org.fxapps.llmfx.Events.SaveFormat;
import org.fxapps.llmfx.Events.SelectedModelEvent;
import org.fxapps.llmfx.Events.StopStreamingEvent;
import org.fxapps.llmfx.Events.UserInputEvent;
import org.w3c.dom.html.HTMLElement;

import io.quarkiverse.fx.views.FxView;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

//TODOS:
// 1. make the models refresh part of the models menu if a separator
// 2. add a deselect all tools menu to the tools menu
// 3. add a deselect all mcp menu to the mcp menu
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
                            padding: 5px !important;
                            padding-left: 10px !important;
                        }
                        .user-message {
                            background-color: lightblue;
                        }
                        .system-message {
                            background-color: #f1f1f1;
                            border-left: 4px solid #0056b3;
                            color: #DDDDDD !important;
                            font-style: italic;
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

    @FXML
    SplitPane spBody;

    @FXML
    TabPane pnlJFX;

    @FXML
    Button btnClearCanvas;

    @FXML
    private Tab canvasTab;

    @FXML
    private Tab reportingTab;

    private Pane canvasPane;

    private GridPane reportingPane;

    private SimpleBooleanProperty holdChatProperty;

    private boolean autoScroll;

    private Tooltip mcpMenuTooltip;

    private Set<String> selectedTools;
    private Set<String> selectedMCPs;

    public void init() {
        this.mcpMenuTooltip = new Tooltip("Select MCP Servers");
        this.canvasPane = new AnchorPane();
        this.reportingPane = new GridPane(5, 5);
        this.selectedTools = new HashSet<>();
        this.selectedMCPs = new HashSet<>();

        canvasTab.setContent(new ScrollPane(canvasPane));
        canvasPane.setTranslateX(5);
        canvasPane.setTranslateY(5);

        reportingTab.setContent(new ScrollPane(reportingPane));
        reportingPane.setTranslateX(5);
        reportingPane.setTranslateY(5);

        pnlJFX.getTabs().addAll(canvasTab, reportingTab);

        holdChatProperty = new SimpleBooleanProperty();
        txtInput.disableProperty().bind(holdChatProperty);
        btnNewChat.disableProperty().bind(holdChatProperty);
        historyList.disableProperty().bind(holdChatProperty);
        btnStop.disableProperty().bind(holdChatProperty.not());
        mcpMenu.setTooltip(mcpMenuTooltip);

        btnClearCanvas.setOnAction(e -> canvasPane.getChildren().clear());
        chatOutput.setOnScroll(e -> autoScroll = false);

        chatOutput.getEngine().loadContent(CHAT_PAGE);

        this.historyList.setOnMouseClicked(e -> {
            var i = historyList.getSelectionModel().getSelectedIndex();
            historySelectedEvent.fire(new HistorySelectedEvent(i));
        });

        this.historyList.getSelectionModel().selectedIndexProperty().addListener((obs, old, n) -> {
            final var i = n.intValue();
            historySelectedEvent.fire(new HistorySelectedEvent(i));
        });

        btnTrashConversation.disableProperty()
                .bind(historyList.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
    }

    @FXML
    void onDownloadImage() {
        // TODO: implement
    }

    @FXML
    void onInputAction() {
        final var input = txtInput.getText();
        if (!input.isBlank()) {
            txtInput.setText("");
            onUserInputEvent.fire(new UserInputEvent(input));
        }
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

    public Set<String> selectedTools() {
        return selectedTools;
    }

    public Set<String> selectedMCPs() {
        return selectedMCPs;
    }

    public void enableMCPMenu(boolean enable) {
        var text = enable ? "MCP Servers are enabled" : "MCP Servers are ignored because Tools are selected";
        mcpMenuTooltip.setText(text);
        mcpMenu.setDisable(!enable);
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

    public void onNewDrawingNodeEvent(@Observes NewDrawingNodeEvent event) {

        Platform.runLater(() -> {
            canvasPane.getChildren().add(event.node());
            pnlJFX.getSelectionModel().select(canvasTab);
            spBody.setDividerPosition(2, 0.4);
        });

    }

    public void onClearReportingEvent(@Observes ClearReportEvent event) {
        Platform.runLater(() -> this.reportingPane.getChildren().clear());
    }

    public void onClearDrawingNodeEvent(@Observes ClearDrawingEvent event) {

        Platform.runLater(() -> {
            if (this.canvasTab.isSelected()) {
                this.canvasPane.getChildren().clear();
            }
            if (this.reportingTab.isSelected()) {                
                this.reportingPane.getChildren().removeAll();
                // workaround to make sure the grid pane is clean
                this.reportingPane = new GridPane(5, 5);
                this.reportingTab.setContent(new ScrollPane(reportingPane));
            }
        });
    }

    public void onNewReportingNodeEvent(@Observes NewReportingNodeEvent evt) {

        Platform.runLater(() -> {
            reportingPane.add(evt.node(), evt.column(), evt.row());
            pnlJFX.getSelectionModel().select(reportingTab);
            spBody.setDividerPosition(2, 0.4);
        });

    }

    public void appendUserMessage(String userMessage) {
        runScriptToAppendMessage("<p>" + userMessage + "</p>", "user");
    }

    public void appendSystemMessage(String systemMessage) {
        runScriptToAppendMessage("<p>" + systemMessage + "</p>", "system");
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
                var isSelected = menu.isSelected();
                if (isSelected) {
                    selectedMCPs.add(mcpServer);

                } else {
                    selectedMCPs.remove(mcpServer);
                }
                mcpMenu.setText("MCP");
                if (!selectedMCPs.isEmpty()) {
                    mcpMenu.setText(mcpMenu.getText() + " (" + selectedMCPs.size() + ")");
                }
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
                        menu.setOnAction(evt -> {
                            final var isSelected = menu.isSelected();
                            if (isSelected) {
                                selectedTools.add(tool);
                            } else {

                                selectedTools.remove(tool);
                            }
                            toolsMenu.setText("Tools");
                            if (!selectedTools.isEmpty()) {
                                toolsMenu.setText(toolsMenu.getText() + " (" + selectedTools.size() + ")");

                            }
                            enableMCPMenu(selectedTools.isEmpty());
                        });
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