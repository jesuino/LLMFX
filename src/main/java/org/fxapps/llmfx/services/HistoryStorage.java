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

    @PostConstruct
    void init() {
        this.chatHistory = new ArrayList<>();
        this.jsonBuilder = JsonbBuilder.create();
        if (appConfig.historyFile().isPresent()) {
            this.historyFile = Path.of(appConfig.historyFile().get());
            if (!Files.exists(historyFile)) {
                try {
                    Files.createDirectories(historyFile.resolve(".."));
                    Files.createFile(historyFile);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to create history file: " + historyFile, e);
                }
            }
        }
    }

    public List<ChatHistory> load() throws IOException {
        if (this.historyFile != null) {
            var jsonContent = Files.readString(historyFile);

            if (!jsonContent.isBlank()) {
                final var loadHistory = Arrays.asList(jsonBuilder.fromJson(jsonContent, ChatHistory[].class));
                this.chatHistory = new ArrayList<>(loadHistory);
            }
        }

        return this.chatHistory;

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

}
