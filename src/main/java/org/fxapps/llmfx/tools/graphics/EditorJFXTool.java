package org.fxapps.llmfx.tools.graphics;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;

public abstract class EditorJFXTool implements JFXTool {

    private SplitPane splitPane;
    private TextArea txtCode;

    void init() {
        txtCode = new TextArea();
        splitPane = new SplitPane();
        splitPane.getItems().addAll(getRenderNode(), txtCode);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8);

        txtCode.textProperty().addListener((obs, oldText, newText) ->

        onEditorChange(newText)

        );
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
