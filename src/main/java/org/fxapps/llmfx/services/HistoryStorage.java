package org.fxapps.llmfx.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fxapps.llmfx.Model.ChatHistory;
import org.fxapps.llmfx.config.AppConfig;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@Singleton
public class HistoryStorage {

    @Inject
    AppConfig appConfig;

    private Path historyFile;

    private Jsonb jsonBuilder;

    private List<ChatHistory> chatHistory;

    private ChatHistory currentConversation;

    @PostConstruct
    void init() throws IOException {
        this.chatHistory = new ArrayList<>();
        this.jsonBuilder = JsonbBuilder.create();
        this.currentConversation = ChatHistory.empty();
        if (appConfig.historyFile().isPresent()) {
            this.historyFile = Path.of(appConfig.historyFile().get());
            if (!Files.exists(historyFile)) {
                Files.createDirectories(historyFile.resolve(".."));
                Files.createFile(historyFile);
            } else {
                var jsonContent = Files.readString(historyFile);
                if (!jsonContent.isBlank()) {
                    final var loadHistory = Arrays.asList(jsonBuilder.fromJson(jsonContent, ChatHistory[].class));
                    this.chatHistory.addAll(loadHistory);
                    if (!chatHistory.isEmpty()) {
                        this.currentConversation = this.chatHistory.getFirst();
                    }
                }
            }
        }
    }

    public List<ChatHistory> getChatHistory() {
        return chatHistory;
    }

    public void save() throws IOException {
        if (this.historyFile != null) {
            var content = jsonBuilder.toJson(this.chatHistory);
            Files.writeString(historyFile, content);
        }
    }

    public ChatHistory getConversation() {
        return this.currentConversation;
    }

    public void newConversation(String title) {
        this.currentConversation = ChatHistory.withTitle(title);
        this.chatHistory.add(this.currentConversation);
    }

    public void clearConversation() {
        this.currentConversation = ChatHistory.empty();
    }

    public void setConversation(ChatHistory selectedHistory) {
        this.currentConversation = selectedHistory;
    }

    public void removeConversation(ChatHistory selectedHistory) {
        this.getChatHistory().remove(selectedHistory);
        if (this.getConversation() == selectedHistory) {
            this.currentConversation = ChatHistory.empty();
        }

    }

}
