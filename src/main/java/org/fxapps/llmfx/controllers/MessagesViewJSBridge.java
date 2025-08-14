package org.fxapps.llmfx.controllers;

import jakarta.inject.Singleton;
/**
 * Contains methods exposed to Javascript
 */
@Singleton
public class MessagesViewJSBridge {

    public void openUrl(String url) {
        // TODO: fire an event to open an URL
        System.out.println("Clicked on " + url);
    }
    
}
