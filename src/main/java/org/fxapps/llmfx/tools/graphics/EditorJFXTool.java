package org.fxapps.llmfx.tools.graphics;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;

public abstract class EditorJFXTool implements JFXTool {

    private SplitPane splitPane;
    private TextArea txtCode;

    void init() {
        var scrollPane = new ScrollPane(getRenderNode());
        scrollPane.setPrefWidth(1200);
        scrollPane.setPrefHeight(900);
        txtCode = new TextArea();
        splitPane = new SplitPane();
        splitPane.getItems().addAll(scrollPane, txtCode);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8);

        txtCode.textProperty().addListener((obs, o, n) -> onEditorChange(n));
    }

    @Override
    public void clear() {
        txtCode.clear();
        clearRenderNode();
    }

    @Override
    public Node getRoot() {
        return splitPane;
    }

    void setEditorContent(String content) {
        txtCode.setText(content);
    }

    abstract Node getRenderNode();

    abstract void clearRenderNode();

    abstract void onEditorChange(String newContent);

}
