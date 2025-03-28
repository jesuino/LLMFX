package org.fxapps.ollamafx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fxapps.ollamafx.controllers.ChatController;
import org.fxapps.ollamafx.events.ChatUpdateEvent;
import org.fxapps.ollamafx.events.ClearChatEvent;
import org.fxapps.ollamafx.events.SelectedModelEvent;
import org.fxapps.ollamafx.events.UserInputEvent;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResponseModel;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.generate.OllamaTokenHandler;
import io.github.ollama4j.models.response.Model;
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
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;

@ApplicationScoped
public class App {

    final String OLLAMA_DEFAULT_URL = "http://localhost:11434";
    final String OLLAMA_DEFAULT_MODEL = "qwen2.5:latest";

    OllamaAPI ollamaAPI;

    @Inject
    FxViewRepository viewRepository;

    @Inject
    Event<ChatUpdateEvent> historyEvent;

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
        final var stage = event.getPrimaryStage();
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
            ollamaAPI.chatStreaming(chatRequest, token -> {
                onGoingTokens.append(token.getMessage().getContent());
                tempMessage.setContent(onGoingTokens.toString());
                showChatHistory();
                if (token.isDone()) {
                    chatHistory.remove(tempMessage);
                }
            });
            showChatHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChatHistory() {
        Platform.runLater(() -> getFormattedHistory());
    }

    private void getFormattedHistory() {
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

}
