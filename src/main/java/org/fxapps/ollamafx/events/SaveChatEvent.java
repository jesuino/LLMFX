package org.fxapps.ollamafx.events;

public class SaveChatEvent {

    public enum Format {
        TEXT, JSON, HTML;
    };

    public SaveChatEvent(Format format) {
        this.format = format;
    }

    private Format format;

    public Format getFormat() {
        return format;
    }

}
