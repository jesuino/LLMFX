package org.fxapps.llmfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.tool.ToolProvider;

public class Model {

    public enum Role {
        ASSISTANT, USER;
    }

    public record Message(String content, Role role) {
        public static Message userMessage(String content) {
            return new Message(content, Role.USER);
        }

        public static Message assistantMessage(String content) {
            return new Message(content, Role.ASSISTANT);
        }
    }

    public record ChatRequest(
            String message,
            List<Message> messages,
            String model,
            Set<Object> tools,
            ToolProvider toolProvider,
            AtomicBoolean stop,
            Consumer<String> onToken,
            Consumer<ChatResponse> onComplete,
            Consumer<Throwable> onError) {
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
