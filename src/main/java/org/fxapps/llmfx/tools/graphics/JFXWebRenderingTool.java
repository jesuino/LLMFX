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
            Render and allow users to visualize HTML or SVG content on a web view.
            """)
    public void renderHTML(@P("The HTML content to be rendered") String html) {
        Platform.runLater(() -> {
            super.setEditorContent(html);
            render(html);
        });

    }

    @Override
    Node getRenderNode() {
        return webView;
    }

    @Override
    void clearRenderNode() {
        render("");
    }

    @Override
    void onEditorChange(String newContent) {
        render(newContent);

    }

    void render(String content) {
        Platform.runLater(() -> this.webView.getEngine().loadContent(content));

    }

}
