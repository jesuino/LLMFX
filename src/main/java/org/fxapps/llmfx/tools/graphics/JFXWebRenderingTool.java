package org.fxapps.llmfx.tools.graphics;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;

@Singleton
public class JFXWebRenderingTool extends EditorJFXTool {
 
    private WebView webView;

    @PostConstruct
    public void init() {
        this.webView = new WebView();
        super.init();
    }

    @Tool("""
            Render and allow users to visualize HTML content. You can use this tool to render HTML content for the user.
            """)
    public void renderHTML(@P("The HTML content to be rendered") String html) {
        Platform.runLater(() -> super.setEditorContent(html));
    }

    @Override
    Node getRenderNode() {        
        return webView;
    }

    @Override
    void clearRenderNode() {
        webView.getEngine().loadContent("");
    }

    @Override
    void onEditorChange(String newContent) {
        webView.getEngine().loadContent(newContent);
    }

}
