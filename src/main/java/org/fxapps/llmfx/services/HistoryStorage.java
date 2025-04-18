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

    @PostConstruct
    void init() {
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
                return Arrays.asList(jsonBuilder.fromJson(jsonContent, ChatHistory[].class));
            }
        }
        return new ArrayList<>();

    }

    public void save(List<ChatHistory> history) throws IOException {
        if (this.historyFile != null) {
            var content = jsonBuilder.toJson(history);
            Files.writeString(historyFile, content);
        }
    }

}
