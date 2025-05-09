package org.fxapps.llmfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.response.ChatResponse;


public class Model {

    public enum Role {
        ASSISTANT, USER, SYSTEM;
    }

    public enum ContentType {
        IMAGE, AUDIO, VIDEO, PDF;
    }

    public record Content(String content, ContentType type, String mimeType) {

        static Content empty() {
            return new Content(null, null, null);
        }

        public boolean isEmpty() {
            return this.equals(empty());
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
