package org.fxapps.llmfx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.response.ChatResponse;

import javafx.scene.image.Image;

public class Model {

    public enum Role {
        ASSISTANT, USER, SYSTEM;
    }

    public enum ContentType {
        IMAGE, AUDIO, VIDEO, PDF;

        public static ContentType fromMimeType(String mimeType) {
            if (mimeType.startsWith("image/")) {
                return IMAGE;
            } else if (mimeType.startsWith("audio/")) {
                return AUDIO;
            } else if (mimeType.startsWith("video/")) {
                return VIDEO;
            } else if (mimeType.equals("application/pdf")) {
                return PDF;
            }
            throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }
    }

    public record Content(Path path, String content, ContentType type, String mimeType) {

        public static Content fromPath(Path path) throws IOException {
            var mimeType = findFileMimeType(path);
            var contentType = ContentType.fromMimeType(mimeType);
            var content = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            return new Content(path, content, contentType, mimeType);
        }

        private static String findFileMimeType(Path path) throws IOException {
            var mimeType = Files.probeContentType(path);
            if(mimeType == null || mimeType.isBlank()) {
                var fileName = path.getFileName().toString().toLowerCase();

                if(fileName.endsWith("pdf")) {
                    mimeType =  "application/pdf";
                }
            }

            return mimeType;
        }

        static Content empty() {
            return new Content(null, null, null, null);
        }

        public boolean isEmpty() {
            return this.equals(empty());
        }

        public Image getPreview() {
            if (type == ContentType.IMAGE) {
                try {
                    return new Image(new FileInputStream(path.toFile()));
                } catch (FileNotFoundException e) {
                    // let it fall back to the default content type
                }
            }
            // TBD: Add an image based on the content type
            return null;

        }

    }

    public record Message(String text, Role role, Optional<Content> content) {
        public static Message userMessage(String text, Optional<Content> content) {
            return new Message(text, Role.USER, content);
        }

        public static Message assistantMessage(String content) {
            return new Message(content, Role.ASSISTANT, Optional.empty());
        }

        public static Message systemMessage(String content) {
            return new Message(content, Role.SYSTEM, Optional.empty());
        }

    }

    public record ChatRequest(
            String message,
            Optional<Content> content,
            List<Message> history,
            String model,
            Set<Object> tools,
            List<McpClient> mcpClients,
            AtomicBoolean stop,
            Consumer<String> onToken,
            Consumer<ChatResponse> onComplete,
            Consumer<Throwable> onError) {

        public boolean isRunning() {
            return !this.stop.get();
        }
    }

    public record ChatHistory(
            String title,
            List<Message> messages) {

        public static ChatHistory empty() {
            return new ChatHistory("", new ArrayList<>());
        }

        public static ChatHistory withTitle(String title) {
            return new ChatHistory(title, new ArrayList<>());
        }

        public ChatHistory mutable() {
            return new ChatHistory(this.title, new ArrayList<>(this.messages));
        }

        public boolean isEmpty() {
            return this.messages.isEmpty();
        }
    }

}
