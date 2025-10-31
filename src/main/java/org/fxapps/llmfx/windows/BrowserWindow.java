package org.fxapps.llmfx.windows;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;

@Singleton
public class BrowserWindow {

    private static final double BROWSER_WIDTH = 800;
    private static final double BROWSER_HEIGHT = 600;
    private Stage browserWindowStage;
    private TabPane browserTabPane;

    @PostConstruct
    void setup() {
        this.browserWindowStage = new Stage();
        this.browserTabPane = new TabPane();
        browserWindowStage.setScene(new Scene(browserTabPane,
                BROWSER_WIDTH,
                BROWSER_HEIGHT));
        browserWindowStage.setMinWidth(BROWSER_WIDTH);
        browserWindowStage.setMinHeight(BROWSER_HEIGHT);
        browserWindowStage.setTitle("Browser");

        browserTabPane.getTabs()
                .addListener((ListChangeListener.Change<? extends Tab> change) -> {
                    if (browserTabPane.getTabs().isEmpty()) {
                        browserWindowStage.hide();
                    }
                });
    }

    public void openURL(String url) {
        showAndFocus();

        browserTabPane.getTabs()
                .stream()
                .filter(tab -> url.equals(tab.getUserData()))
                .findFirst()
                .ifPresentOrElse(
                        tab -> browserTabPane.getSelectionModel().select(tab),
                        () -> openNewTab(url));
    }

    void showAndFocus() {
        browserWindowStage.show();
        browserWindowStage.setIconified(false);
        browserWindowStage.toFront();
        browserWindowStage.requestFocus();
    }

    void openNewTab(String url) {
        var newTab = new Tab();
        var webView = new WebView();
        newTab.setUserData(url);
        webView.getEngine().load(url);
        newTab.setContent(webView);
        newTab.setText("Loading " + url + "...");
        newTab.setClosable(true);
        webView.getEngine()
                .getLoadWorker()
                .stateProperty()
                .addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        newTab.setText(webView.getEngine().getTitle());
                    }
                });
        browserTabPane.getTabs().add(newTab);
        browserTabPane.getSelectionModel().select(newTab);
    }

}
