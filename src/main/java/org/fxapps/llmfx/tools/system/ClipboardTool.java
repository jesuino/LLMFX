package org.fxapps.llmfx.tools.system;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

@Singleton
public class ClipboardTool {

    @Tool("Copies the given text to the system clipboard")
    public void copyToClipboard(@P("The text to be copied") String text) {
        Platform.runLater(() -> {
            var content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        });
    }

    @Tool("Reads the text content from the system clipboard")
    public String readFromClipboard() {
        var future = new CompletableFuture<String>();
        Platform.runLater(() -> {
            var clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                future.complete(clipboard.getString());
            } else {
                future.complete("Clipboard is empty or does not contain text.");
            }
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "Error reading clipboard: " + e.getMessage();
        }
    }
}
