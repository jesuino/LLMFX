package org.fxapps.llmfx;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.fxapps.llmfx.Events.ChatUpdateEvent;
import org.fxapps.llmfx.Events.DeleteConversationEvent;
import org.fxapps.llmfx.Events.HistorySelectedEvent;
import org.fxapps.llmfx.Events.NewChatEvent;
import org.fxapps.llmfx.Events.RefreshModelsEvent;
import org.fxapps.llmfx.Events.SaveChatEvent;
import org.fxapps.llmfx.Events.SelectedModelEvent;
import org.fxapps.llmfx.Events.StopStreamingEvent;
import org.fxapps.llmfx.Events.UserInputEvent;
import org.fxapps.llmfx.Model.ChatHistory;
import org.fxapps.llmfx.Model.Message;
import org.fxapps.llmfx.Model.Role;
import org.fxapps.llmfx.config.AppConfig;
import org.fxapps.llmfx.config.LLMConfig;
import org.fxapps.llmfx.controllers.ChatController;
import org.fxapps.llmfx.controllers.ChatMessagesView;
import org.fxapps.llmfx.services.ChatService;
import org.fxapps.llmfx.services.HistoryStorage;
import org.fxapps.llmfx.services.MCPClientRepository;
import org.fxapps.llmfx.services.OpenAiService;
import org.fxapps.llmfx.tools.ToolsInfo;
import org.jboss.logging.Logger;

import atlantafx.base.theme.PrimerLight;
import dev.langchain4j.mcp.McpToolProvider;
import io.quarkiverse.fx.FxPostStartupEvent;
import io.quarkiverse.fx.RunOnFxThread;
import io.quarkiverse.fx.views.FxViewData;
import io.quarkiverse.fx.views.FxViewRepository;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// TODO: Support Image upload and response!
@Singleton
public class App {

    Logger logger = Logger.getLogger(App.class);

    @Inject
    FxViewRepository viewRepository;

    @Inject
    Event<ChatUpdateEvent> chatUpdateEvent;

    @Inject
    AlertsHelper alertsHelper;

    @Inject
    ChatService chatService;

    @Inject
    LLMConfig llmConfig;

    @Inject
    OpenAiService openApiService;

    @Inject
    MCPClientRepository mcpClientRepository;

    @Inject
    HistoryStorage historyStorage;

    @Inject
    AppConfig appConfig;

    @Inject
    ChatMessagesView chatMessagesView;

    @Inject
    ToolsInfo toolsInfo;

    private ChatController chatController;

    private FxViewData chatViewData;

    private Stage stage;

    private String selectedModel;

    private Stack<AtomicBoolean> stopStreamingStack;

    void onPostStartup(@Observes final FxPostStartupEvent event) throws Exception {

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        // Application.setUserAgentStylesheet(new
        // PrimerDark().getUserAgentStylesheet());

        this.chatViewData = viewRepository.getViewData("Chat");
        this.chatController = chatViewData.getController();
        this.stage = event.getPrimaryStage();
        this.stopStreamingStack = new Stack<>();
        final var chatView = (Parent) chatViewData.getRootNode();
        final var scene = new Scene(chatView);

        stage.setAlwaysOnTop(appConfig.alwaysOnTop().orElse(true));
        stage.setMinWidth(700);
        stage.setMinHeight(400);
        stage.setOnCloseRequest(noop -> {
            logger.info("Closing application...");
            saveHistory();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.setTitle("LLM FX: A desktop App for LLM Servers");
        stage.show();

        Platform.runLater(() -> {
            chatController.init();
            refreshModels();
            chatController.setMCPServers(mcpClientRepository.mcpServers());
            chatController.setTools(toolsInfo.getToolsCategoryMap());

            if (!historyStorage.getChatHistory().isEmpty()) {
                updateHistoryList();
                showChatMessages();
            }
        });
    }

    private void refreshModels() {
        List<String> tryList = null;
        try {
            tryList = openApiService.listModels();
        } catch (RuntimeException e) {
            logger.error("Error listing models", e);
            alertsHelper.showError("Problem with the LLM Server",
                    "Could not list models from the LLM Server",
                    "Error when trying to list models from the LLM Server. Exiting...");
            System.exit(0);
        }

        final var modelsList = tryList;
        if (modelsList.isEmpty()) {
            alertsHelper.showError("No model",
                    "No Model is available on the server",
                    "No Model found, Check if the server has at least one model available for use. Exiting...");
            System.exit(0);
        }

        chatController.fillModels(modelsList);

        var currentModel = modelsList.stream()
                .filter(m -> m.equals(selectedModel))
                .findAny()
                .or(() -> modelsList.stream()
                        .filter(m -> m.equals(llmConfig.model().orElse("")))
                        .findAny());
        if (currentModel.isPresent()) {
            chatController.setSelectedModel(currentModel.get());
        } else {
            logger.info("No model is set as default, using a random model");
            chatController.setSelectedModel(modelsList.get(0));
        }
    }

    @RunOnFxThread
    void onModelSelected(@Observes SelectedModelEvent selectedModelEvent) {
        this.selectedModel = selectedModelEvent.model();
    }

    @RunOnFxThread
    void onClearChat(@Observes NewChatEvent evt) {
        this.chatController.clearChatHistory();
        this.historyStorage.clearConversation();
    }

    @RunOnFxThread
    void onHistorySelected(@Observes HistorySelectedEvent evt) {
        if (evt.index() == -1) {
            return;
        }
        var selectedHistory = historyStorage.getChatHistory().get(evt.index());
        if (this.historyStorage.getConversation() != selectedHistory) {
            this.historyStorage.setConversation(selectedHistory);
            showChatMessages();
        }

    }

    @RunOnFxThread
    void onHistoryDeleted(@Observes DeleteConversationEvent evt) {
        var selectedHistory = historyStorage.getChatHistory().get(evt.index());
        if (selectedHistory != null) {
            historyStorage.getChatHistory().remove(selectedHistory);
            updateHistoryList();
            chatController.clearChatHistory();
        }
    }

    void onRefreshModels(@Observes RefreshModelsEvent evt) {
        refreshModels();
    }

    void onStopStreaming(@Observes StopStreamingEvent stopStreamingEvent) {
        stopStreamingStack.pop().set(true);
        chatController.holdChatProperty().set(!stopStreamingStack.isEmpty());
    }

    public void onUserInput(@Observes UserInputEvent userInput) {
        final var userMessage = Message.userMessage(userInput.text(), userInput.content());
        if (this.historyStorage.getConversation().isEmpty()) {
            this.historyStorage.newConversation(userMessage.text());
            llmConfig.systemMessage().ifPresent(systemMessage -> {
                var message = Message.systemMessage(systemMessage);
                this.historyStorage.getConversation().messages().add(message);
            });
            updateHistoryList();
        }
        historyStorage.getConversation().messages().add(userMessage);
        saveHistory();

        var toolProvider = McpToolProvider.builder()
                .mcpClients(chatController.selectedMCPs().stream()
                        .map(mcpClientRepository::getMcpClient).toList())
                .build();

        var stopFlag = new AtomicBoolean(false);
        var selectedTools = chatController.selectedTools();
        var tools = toolsInfo.getToolsMap().entrySet().stream()
                .filter(e -> selectedTools.contains(e.getKey()))
                .map(e -> e.getValue())
                .collect(Collectors.toSet());

        historyStorage.getConversation().messages().add(Message.assistantMessage(""));
        chatController.holdChatProperty().set(true);
        stopStreamingStack.push(stopFlag);

        showChatMessages();
        var request = new Model.ChatRequest(
                userInput.text(),
                userInput.content(),
                historyStorage.getConversation().messages(),
                selectedModel,
                tools,
                toolProvider,
                stopFlag,
                token -> {
                    Platform.runLater(() -> {
                        final var previous = historyStorage.getConversation().messages().removeLast();
                        var updatedMessage = Message.assistantMessage(previous.text() + token);
                        historyStorage.getConversation().messages().add(updatedMessage);
                        chatMessagesView.streamAssistantMessage(updatedMessage);
                    });

                },
                r -> {
                    saveHistory();
                    stopStreamingStack.remove(stopFlag);
                    chatController.holdChatProperty().set(false);
                },
                e -> {
                    stopStreamingStack.remove(stopFlag);
                    logger.error("Error during message streaming", e);
                    Platform.runLater(() -> alertsHelper.showError("Error",
                            "There was an error during the conversation",
                            "The following error happened: " + e.getMessage()));
                    chatController.holdChatProperty().set(false);
                });
        chatService.chatAsync(request);

    }

    public void saveChat(@Observes SaveChatEvent saveChatEvent) {
        var content = switch (saveChatEvent.saveFormat()) {
            case HTML -> chatController.getChatHistoryHTML();
            case JSON -> historyStorage.getConversation().messages()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",", "[", "]"));
            case TEXT -> historyStorage.getConversation().messages()
                    .stream()
                    .map(m -> m.role() + ": " + m.text())
                    .collect(Collectors.joining("\n"));
        };
        alertsHelper.showSaveFileChooser("Save chat history", saveChatEvent.saveFormat().name().toLowerCase())
                .ifPresent(dest -> {
                    try {
                        Files.writeString(dest.toPath(), content);
                    } catch (IOException e) {
                        logger.error("Error saving file", e);
                        alertsHelper.showError("Error", "Error saving chat history", "Error: " + e.getMessage());
                    }
                });
    }

    private void updateHistoryList() {
        Platform.runLater(() -> chatController
                .setHistoryItems(historyStorage.getChatHistory().stream().map(ChatHistory::title).toList()));
    }

    private void showChatMessages() {
        Platform.runLater(() -> {
            chatController.clearChatHistory();
            chatController.hideWelcomeMessage();
            historyStorage.getConversation().messages().stream().forEach(message -> {
                Consumer<Message> messageConsumer = switch (message.role()) {
                    case Role.USER -> chatMessagesView::appendUserMessage;
                    case Role.SYSTEM -> chatMessagesView::appendSystemMessage;
                    case Role.ASSISTANT -> chatMessagesView::appendAssistantMessage;
                };
                messageConsumer.accept(message);
            });
        });
    }

    private void saveHistory() {
        try {
            historyStorage.save();
        } catch (IOException e) {
            logger.warn("Error saving chat history", e);
        }
    }

}
