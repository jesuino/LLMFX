package org.fxapps.ollamafx.events;

public class ChatUpdateEvent {

    private String chat;

    public ChatUpdateEvent(String history) {
        this.chat = history;
    }

    public String getChat() {
        return chat;
    }

}
