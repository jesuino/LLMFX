package org.fxapps.llmfx.controllers;

import org.fxapps.llmfx.tools.system.ClipboardTool;
import org.fxapps.llmfx.windows.BrowserWindow;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;

/**
 * Contains methods exposed to Javascript
 */
@Singleton
public class MessagesViewJSBridge {

    @Inject
    BrowserWindow browserWindow;

    @Inject
    ClipboardTool clipboardTool;

    public void openUrl(String url) {
        Platform.runLater(() -> browserWindow.openURL(url));
    }

    public void copyToClipboard(String text) {
        clipboardTool.copyToClipboard(text);
    }

}
