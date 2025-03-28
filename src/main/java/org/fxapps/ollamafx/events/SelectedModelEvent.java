package org.fxapps.ollamafx.events;

public class SelectedModelEvent {

    String model;

    public SelectedModelEvent(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}
