package org.fxapps.llmfx;

public class Events {

    private Events() {
    }

    public enum SaveFormat {
        TEXT, JSON, HTML;
    };

    public record ChatUpdateEvent(String chat) {
    }

    public record ClearChatEvent() {
    }

    public record MCPServerSelectEvent(String name, boolean isSelected) {
    }

    public record ToolSelectEvent(String name, boolean isSelected) {
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

}
