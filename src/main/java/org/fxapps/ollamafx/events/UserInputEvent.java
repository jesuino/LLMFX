package org.fxapps.ollamafx.events;

public class UserInputEvent {
    String text;

    public UserInputEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
