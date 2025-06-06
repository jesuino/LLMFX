package org.fxapps.llmfx;

import java.util.Optional;

import org.fxapps.llmfx.Model.Content;

public class Events {

    private Events() {
    }

    public enum SaveFormat {
        TEXT, JSON, HTML
    }

    public record ChatUpdateEvent(String chat) {
    }

    public record NewChatEvent() {
    }

    public record ReloadMessageEvent() {
    }

    public record SaveChatEvent(SaveFormat saveFormat) {
    }

    public record SelectedModelEvent(String model) {
    }

    public record StopStreamingEvent() {
    }

    public record UserInputEvent(String text, Optional<Content> content) {
    }

    public record RefreshModelsEvent() {
    }

    public record HistorySelectedEvent(int index) {
    }

    public record DeleteConversationEvent(int index) {
    }

}
