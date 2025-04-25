package org.fxapps.llmfx.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.fxapps.llmfx.AlertsHelper;
import org.fxapps.llmfx.Events.ClearDrawingEvent;
import org.fxapps.llmfx.Events.ClearReportEvent;
import org.fxapps.llmfx.Events.DeleteConversationEvent;
import org.fxapps.llmfx.Events.HistorySelectedEvent;
import org.fxapps.llmfx.Events.NewChatEvent;
import org.fxapps.llmfx.Events.NewDrawingNodeEvent;
import org.fxapps.llmfx.Events.NewHTMLContentEvent;
import org.fxapps.llmfx.Events.NewReportingNodeEvent;
import org.fxapps.llmfx.Events.RefreshModelsEvent;
import org.fxapps.llmfx.Events.SaveChatEvent;
import org.fxapps.llmfx.Events.SaveFormat;
import org.fxapps.llmfx.Events.SelectedModelEvent;
import org.fxapps.llmfx.Events.StopStreamingEvent;
import org.fxapps.llmfx.Events.UserInputEvent;
import org.fxapps.llmfx.FXUtils;
import org.fxapps.llmfx.config.AppConfig;

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
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

//TODOS:
// 1. make the models refresh part of the models menu if a separator
// 2. add a deselect all tools menu to the tools menu
// 3. add a deselect all mcp menu to the mcp menu
@FxView
@Singleton
public class ChatController {

    private static final String MCP_LABEL = "MCP";

    private static final String TOOLS_LABEL = "Tools";

    @Inject
    private AppConfig appConfig;

    @Inject
    Event<UserInputEvent> onUserInputEvent;

    @Inject
    ChatMessagesView chatMessagesView;

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
    TabPane graphicsPane;

    @FXML
    Button btnClearCanvas;

    @FXML
    private Tab canvasTab;

    @FXML
    private Tab reportingTab;

    @FXML
    private Tab webViewTab;

    @FXML
    WebView webContentView;

    @FXML
    private AnchorPane canvasPane;

    @FXML
    private GridPane reportingPane;

    private SimpleBooleanProperty holdChatProperty;

    private MenuItem clearToolsMenuItem;

    public void init() {
        this.clearToolsMenuItem = new MenuItem("Clear all tools");
        this.chatMessagesView.init(chatOutput);

        clearToolsMenuItem.setOnAction(e -> {
            toolsMenu.getItems().forEach(item -> {
                if (item instanceof Menu catMenu) {
                    catMenu.getItems().forEach(it -> {
                        if (it instanceof CheckMenuItem checkMenuItem) {
                            checkMenuItem.setSelected(false);
                        }
                    });
                }
            });
            toolsMenu.setText(TOOLS_LABEL);
        });

        graphicsPane.getTabs().clear();

        holdChatProperty = new SimpleBooleanProperty();
        txtInput.disableProperty().bind(holdChatProperty);
        btnNewChat.disableProperty().bind(holdChatProperty);
        historyList.disableProperty().bind(holdChatProperty);
        btnStop.disableProperty().bind(holdChatProperty.not());

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

        if (appConfig.historyFile().isEmpty()) {
            spBody.setDividerPositions(new double[] { 0, 1d });
        }
    }

    @FXML
    void onDownloadImage() {
        var selectedTab = graphicsPane.getSelectionModel().getSelectedItem();

        var image = selectedTab.getContent().snapshot(new SnapshotParameters(), null);

        alertsHelper.showFileChooser("Save image", "png").ifPresent(f -> {
            try {
                var bufferedImage = FXUtils.fromFXImage(image);
                ImageIO.write(bufferedImage, "png", f);
            } catch (IOException e) {
                alertsHelper.showError("Error", "Error saving image", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    void clearCurrentGraphicsTab() {
        var selectedTab = graphicsPane.getSelectionModel().getSelectedItem();
        if (this.canvasTab == selectedTab) {
            this.canvasPane.getChildren().clear();
        }
        if (this.reportingTab == selectedTab) {
            this.reportingPane.getChildren().removeAll();
            this.reportingPane.getChildren().clear();
        }
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
        return toolsMenu.getItems().stream().flatMap(item -> {
            if (item instanceof Menu catMenu) {
                return catMenu.getItems()
                        .stream()
                        .filter(it -> it instanceof CheckMenuItem check && check.isSelected())
                        .map(it -> ((CheckMenuItem) it).getText());
            }
            return Stream.empty();
        }).collect(Collectors.toSet());
    }

    public Set<String> selectedMCPs() {
        return mcpMenu.getItems()
                .stream()
                .filter(item -> item instanceof CheckMenuItem check && check.isSelected())
                .map(MenuItem::getText)
                .collect(Collectors.toSet());
    }

    public BooleanProperty holdChatProperty() {
        return holdChatProperty;
    }

    public void setHistoryItems(Collection<String> items) {
        historyList.getItems().clear();
        historyList.getItems().addAll(items);
        historyList.getSelectionModel().selectLast();
    }

    public void fillModels(List<String> modelsNames) {
        cmbModels.setItems(FXCollections.observableList(modelsNames));
    }

    public void onNewDrawingNodeEvent(@Observes NewDrawingNodeEvent event) {
        Platform.runLater(() -> {
            if (!graphicsPane.getTabs().contains(canvasTab)) {
                graphicsPane.getTabs().add(canvasTab);
            }
            canvasPane.getChildren().add(event.node());
            graphicsPane.getSelectionModel().select(canvasTab);
            spBody.setDividerPosition(1, 0.4);
        });
    }

    public void onNewHTMLContentEvent(@Observes NewHTMLContentEvent event) {
        Platform.runLater(() -> {
            if (!graphicsPane.getTabs().contains(webViewTab)) {
                graphicsPane.getTabs().add(webViewTab);
            }
            webContentView.getEngine().loadContent(event.htmlContent());
            graphicsPane.getSelectionModel().select(webViewTab);
            spBody.setDividerPosition(1, 0.4);
        });
    }

    public void onNewReportingNodeEvent(@Observes NewReportingNodeEvent evt) {
        Platform.runLater(() -> {
            if (!graphicsPane.getTabs().contains(reportingTab)) {
                graphicsPane.getTabs().add(reportingTab);
            }
            reportingPane.add(evt.node(), evt.column(), evt.row());
            graphicsPane.getSelectionModel().select(reportingTab);
            spBody.setDividerPosition(1, 0.4);
        });
    }

    public void onClearReportingEvent(@Observes ClearReportEvent event) {
        Platform.runLater(() -> this.reportingPane.getChildren().clear());
    }

    public void onClearDrawingNodeEvent(@Observes ClearDrawingEvent event) {
        Platform.runLater(() -> this.canvasPane.getChildren().clear());
    }

    public void setSelectedModel(String model) {
        cmbModels.getSelectionModel().select(model);
    }

    public void clearChatHistoy() {
        vbWelcomeMessage.setVisible(true);
        chatMessagesView.clearChatHistory();
    }

    public String getChatHistoryHTML() {
        return chatMessagesView.getChatHistoryHTML();
    }

    public void setMCPServers(Collection<String> mcpServers) {
        mcpServers.stream().map(mcpServer -> {
            var menu = new CheckMenuItem(mcpServer);
            menu.selectedProperty().addListener((obs, old, n) -> mcpMenu
                    .setText(MCP_LABEL +
                            (selectedMCPs().isEmpty()
                                    ? ""
                                    : " (" + selectedMCPs().size() + ")"))

            );
            return menu;
        }).forEach(mcpMenu.getItems()::add);

    }

    public void setTools(Map<String, List<String>> toolsCat) {
        toolsCat.entrySet()
                .stream()
                .map(e -> {
                    Menu mnCat = new Menu(e.getKey());
                    e.getValue().stream().map(tool -> {
                        var menu = new CheckMenuItem(tool);
                        menu.selectedProperty().addListener((obs, old, n) -> {
                            toolsMenu.setText(TOOLS_LABEL
                                    + (selectedTools().isEmpty()
                                            ? ""
                                            : " (" + selectedTools().size() + ")"));

                            mcpMenu.setDisable(!selectedTools().isEmpty());

                        });
                        return menu;

                    }).forEach(mnCat.getItems()::add);
                    return mnCat;
                }).forEach(toolsMenu.getItems()::add);
        toolsMenu.getItems().add(new SeparatorMenuItem());
        toolsMenu.getItems().add(clearToolsMenuItem);
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

    public void appendUserMessage(String content) {
        vbWelcomeMessage.setVisible(false);
        chatMessagesView.setAutoScroll(true);
        chatMessagesView.appendUserMessage(content);
    }

    public void appendSystemMessage(String content) {
        vbWelcomeMessage.setVisible(false);
        chatMessagesView.appendSystemMessage(content);
    }

    public void appendAssistantMessage(String htmlMessage) {
        vbWelcomeMessage.setVisible(false);
        chatMessagesView.appendAssistantMessage(htmlMessage);
    }
}