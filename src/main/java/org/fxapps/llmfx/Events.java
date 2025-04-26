package org.fxapps.llmfx;

import javafx.scene.Node;

public class Events {

    private Events() {
    }

    public enum SaveFormat {
        TEXT, JSON, HTML;
    };

    public record ChatUpdateEvent(String chat) {
    }

    public record NewChatEvent() {
    }

    public record SaveChatEvent(SaveFormat saveFormat) {
    }

    public record SelectedModelEvent(String model) {
    }

    public record StopStreamingEvent() {
    }

    public record UserInputEvent(String text) {
    }

    public record RefreshModelsEvent() {
    }

    public record HistorySelectedEvent(int index) {
    }

    public record DeleteConversationEvent(int index) {
    }

    public record NewReportingNodeEvent(Node node, int column, int row, int colspan, int rowspan) {
    }

    public record ClearReportEvent() {
    }

    public record NewHTMLContentEvent(String htmlContent) {
    }

    public record New3DContentEvent(Node node) {
    }

    public record DrawingStartedEvent() {
    }

}
