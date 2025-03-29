package org.fxapps.ollamafx;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fxapps.ollamafx.controllers.ChatController;
import org.fxapps.ollamafx.events.ChatUpdateEvent;
import org.fxapps.ollamafx.events.ClearChatEvent;
import org.fxapps.ollamafx.events.SaveChatEvent;
import org.fxapps.ollamafx.events.SelectedModelEvent;
import org.fxapps.ollamafx.events.UserInputEvent;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.quarkiverse.fx.FxPostStartupEvent;
import io.quarkiverse.fx.RunOnFxThread;
import io.quarkiverse.fx.views.FxViewData;
import io.quarkiverse.fx.views.FxViewRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@ApplicationScoped
public class App {

    final String OLLAMA_DEFAULT_URL = "http://localhost:11434";
    final String OLLAMA_DEFAULT_MODEL = "qwen2.5:latest";

    OllamaAPI ollamaAPI;

    @Inject
    FxViewRepository viewRepository;

    @Inject
    Event<ChatUpdateEvent> historyEvent;

    @Inject
    AlertsHelper alertsHelper;

    @ConfigProperty(name = "ollamafx.url", defaultValue = OLLAMA_DEFAULT_URL)
    String ollamaUrl;

    @ConfigProperty(name = "ollamafx.model", defaultValue = OLLAMA_DEFAULT_MODEL)
    String ollamaModel;
    private ChatController chatController;
    private Parser markDownParser;
    private HtmlRenderer markdownRenderer;

    List<OllamaChatMessage> chatHistory = new ArrayList<>();
    List<String> modelsList;
    private FxViewData chatViewData;
    private String currentModel;

    private Map<OllamaChatMessage, String> htmlMessageCache;
    private Stage stage;

    void onPostStartup(@Observes final FxPostStartupEvent event) throws Exception {
        this.ollamaAPI = new OllamaAPI(ollamaUrl);
        this.chatViewData = viewRepository.getViewData("Chat");
        this.chatController = chatViewData.getController();
        this.modelsList = ollamaAPI.listModels().stream().map(m -> m.getModel())
                .toList();

        this.markDownParser = Parser.builder().build();
        this.markdownRenderer = HtmlRenderer.builder().build();
        this.htmlMessageCache = new HashMap<>();

        final var rootNode = (Parent) chatViewData.getRootNode();
        final var scene = new Scene(rootNode);
        this.stage = event.getPrimaryStage();
        stage.setScene(scene);
        stage.setTitle("OllamaFX: A desktop App for Ollama");
        stage.show();

        chatController = chatViewData.<ChatController>getController();
        chatController.initializeWebView();
        chatController.fillModels(modelsList);

        if (modelsList.stream().anyMatch(m -> m.equals(ollamaModel))) {
            chatController.setSelectedModel(ollamaModel);
        } else {
            chatController.chatDisableProperty().set(true);
        }

    }

    @RunOnFxThread
    public void onModelSelected(@Observes SelectedModelEvent selectedModelEvent) {
        System.out.println("Selected model: " + selectedModelEvent.getModel());
        this.currentModel = selectedModelEvent.getModel();
    }

    @RunOnFxThread
    public void onClearChat(@Observes ClearChatEvent evt) {
        this.chatHistory.clear();
        this.chatController.clearChatHistoy();
        this.htmlMessageCache.clear();
    }

    public void onUserInput(@ObservesAsync UserInputEvent userInput) {
        final var chatBuilder = OllamaChatRequestBuilder.getInstance(this.currentModel);
        final var chatRequest = chatBuilder.withMessages(chatHistory)
                .withMessage(OllamaChatMessageRole.USER, userInput.getText()).build();

        showChatHistory();

        try {
            final var onGoingTokens = new StringBuffer();
            // workaround for: https://github.com/ollama4j/ollama4j/issues/115
            final var tempMessage = new OllamaChatMessage();
            tempMessage.setContent("...");
            chatHistory.add(tempMessage);
            showChatHistory();
            chatController.chatDisableProperty().set(true);
            ollamaAPI.chatStreaming(chatRequest, token -> {
                onGoingTokens.append(token.getMessage().getContent());
                tempMessage.setContent(onGoingTokens.toString());
                showChatHistory();
                if (token.isDone()) {
                    chatHistory.remove(tempMessage);
                    chatController.chatDisableProperty().set(false);
                }
            });
            showChatHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChatHistory() {
        Platform.runLater(() -> updateHistory());
    }

    private void updateHistory() {
        chatController.clearChatHistoy();
        chatHistory.stream().forEach(message -> {
            if (OllamaChatMessageRole.USER == message.getRole()) {
                chatController.appendUserMessage(message.getContent());
            } else {
                var htmlMessage = htmlMessageCache.computeIfAbsent(message,
                        messageToParse -> parseMarkdowToHTML(messageToParse.getContent()));
                chatController.appendAssistantMessage(htmlMessage);
            }
        });
    }

    private String parseMarkdowToHTML(String markdown) {
        var parsedContent = markDownParser.parse(markdown);
        return markdownRenderer.render(parsedContent);
    }

    public void saveChat(@Observes SaveChatEvent saveChatEvent) {
        var content = switch (saveChatEvent.getFormat()) {
            case HTML -> chatController.getChatHistoryHTML();
            case JSON -> getHistoryAsJson();
            case TEXT -> getHistoryAsText();
        };
        var  fileChooser = new FileChooser();
        var dest = fileChooser.showSaveDialog(stage);

        try {
            Files.writeString(dest.toPath(), content);
        } catch (IOException e) {
            alertsHelper.showError("Error", "Error saving chat History", "Error: " + e.getMessage());
        }

    }

    String getHistoryAsJson() {
        return chatHistory.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "]"));

    }

    String getHistoryAsText() {
        return chatHistory.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

}
