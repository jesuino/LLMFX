package org.fxapps.llmfx.tools.graphics;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.web.WebView;

@Singleton
public class JFXWebRenderingTool {

    private WebView webView;

    public void setWebView(WebView webView) {
        this.webView = webView;

    }

    @Tool("""
            Render and allow users to visualize HTML content. You can use this tool to render HTML content for the user.
            Make sure the HTML contains all the CSS and javascript used by it inside the HTML content, external files will not work.
            """)
    public void renderHTML(@P("The HTML content to be rendered") String html) {
        Platform.runLater(() -> this.webView.getEngine().loadContent(html));
    }

}
