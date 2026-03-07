package org.fxapps.llmfx.tools.graphics;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;

@Singleton
public class JFXWebRenderingTool implements JFXTool {
 
    private WebView webView;

    @PostConstruct
    public void init() {
        this.webView = new WebView();
    }

    public void clear() {
        this.webView.getEngine().loadContent("");
    }

    public Node getRoot() {
        return webView;
    }

    @Tool("""
            Render and allow users to visualize HTML content. You can use this tool to render HTML content for the user.
            """)
    public void renderHTML(@P("The HTML content to be rendered") String html) {
        Platform.runLater(() -> this.webView.getEngine().loadContent(html));
    }

}
