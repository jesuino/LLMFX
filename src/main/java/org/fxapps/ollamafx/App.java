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
import org.fxapps.ollamafx.App.Message;
import org.fxapps.ollamafx.controllers.ChatController;
import org.fxapps.ollamafx.events.ChatUpdateEvent;
import org.fxapps.ollamafx.events.ClearChatEvent;
import org.fxapps.ollamafx.events.SaveChatEvent;
import org.fxapps.ollamafx.events.SelectedModelEvent;
import org.fxapps.ollamafx.events.UserInputEvent;
import org.fxapps.ollamafx.services.ChatModelFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.output.Response;
import io.github.ollama4j.OllamaAPI;
import io.quarkiverse.fx.FxPostStartupEvent;
import io.quarkiverse.fx.RunOnFxThread;
import io.quarkiverse.fx.views.FxViewData;
import io.quarkiverse.fx.views.FxViewRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@ApplicationScoped
public class App {

    final String OLLAMA_MODEL_ID_CONFIG = "quarkus.langchain4j.ollama.chat-model.model-id";

    final String OLLAMA_BASE_URL_CONFIG = "quarkus.langchain4j.ollama.base-url";

    OllamaAPI ollamaAPI;

    @Inject
    FxViewRepository viewRepository;

    @Inject
    Event<ChatUpdateEvent> historyEvent;

    @Inject
    AlertsHelper alertsHelper;

    @ConfigProperty(name = "ollama.url", defaultValue = "http://localhost:11434")
    String ollamaUrl;

    @ConfigProperty(name = "ollama.model", defaultValue = "qwen2.5:latest")
    String ollamaModel;

    @Inject
    ChatModelFactory modelFactory;
    private ChatController chatController;
    private Parser markDownParser;
    private HtmlRenderer markdownRenderer;

    private StreamingChatLanguageModel model;
    List<Message> chatHistory = new ArrayList<>();
    List<String> modelsList;
    private FxViewData chatViewData;

    enum Role {
        ASSISTANT, USER;
    }

    record Message(String content, Role role) {
    }

    private Map<Message, String> htmlMessageCache;
    private Stage stage;


    void onPostStartup(@Observes final FxPostStartupEvent event) throws Exception {
        this.ollamaAPI = new OllamaAPI(ollamaUrl);

        this.chatViewData = viewRepository.getViewData("Chat");
        this.chatController = chatViewData.getController();
        this.modelsList = ollamaAPI.listModels().stream().map(m -> m.getModel()).toList();
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
        this.model = modelFactory.getModel(ollamaUrl, selectedModelEvent.getModel());
    }

    @RunOnFxThread
    public void onClearChat(@Observes ClearChatEvent evt) {
        this.chatHistory.clear();
        this.chatController.clearChatHistoy();
        this.htmlMessageCache.clear();
    }

    public void onUserInput(@ObservesAsync UserInputEvent userInput) {
        final var userMessage = new Message(userInput.getText(), Role.USER);
        chatHistory.add(userMessage);
        try {
            final var tempMessage = new Message("", Role.ASSISTANT);
            chatHistory.add(tempMessage);
            showChatHistory();
            chatController.chatDisableProperty().set(true);

            var chatMessages = chatHistory.stream().map(m -> {
                return switch (m.role()) {
                    case USER -> new UserMessage(m.content());
                    case ASSISTANT -> new AiMessage(m.content());
                };
            }).toList();
            var request = ChatRequest.builder().messages(chatMessages).build();
            this.model.chat(request, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String token) {
                    final var previous = chatHistory.removeLast();
                    Platform.runLater(() -> chatHistory.add(new Message(previous.content() + token, Role.ASSISTANT)));
                    showChatHistory();
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    chatController.chatDisableProperty().set(false);
                }

                @Override
                public void onError(Throwable error) {
                    chatController.chatDisableProperty().set(false);
                }
            });

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
            if (Role.USER == message.role()) {
                chatController.appendUserMessage(message.content());
            } else {
                var htmlMessage = htmlMessageCache.computeIfAbsent(message,
                        messageToParse -> parseMarkdowToHTML(messageToParse.content()));
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
        var fileChooser = new FileChooser();
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
                .map(m -> m.role() + ": " + m.content())
                .collect(Collectors.joining("\n"));
    }
}
