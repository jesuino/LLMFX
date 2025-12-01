package org.fxapps.llmfx.tools.graphics;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class EditorJFXTool implements JFXTool {

    private SplitPane splitPane;
    private TextArea txtCode;
    private Label lblError;
    private String lastContent;

    void init() {
        var scrollPane = new ScrollPane(getRenderNode());
        var btnRun = new Button(">");
        lblError = new Label();
        scrollPane.setPrefWidth(1200);
        scrollPane.setPrefHeight(900);
        txtCode = new TextArea();
        splitPane = new SplitPane();
        splitPane.getItems().addAll(scrollPane, new StackPane(txtCode, btnRun, lblError));
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.9);

        lblError.setTextFill(Color.RED);
        lblError.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, 10));
        btnRun.setFont(Font.font(9));

        StackPane.setAlignment(btnRun, Pos.TOP_RIGHT);
        StackPane.setMargin(btnRun, new Insets(10));
        StackPane.setAlignment(lblError, Pos.BOTTOM_LEFT);
        StackPane.setMargin(lblError, new Insets(10));

        txtCode.setOnKeyReleased(event -> {
            if (lastContent != null && !txtCode.getText().equals(lastContent)) {

                txtCode.setStyle("-fx-control-inner-background: #757575ff;");
                setMessage("changes not saved");
            }
        });

        btnRun.setOnAction(e -> {
            setMessage("");
            onEditorChange(txtCode.getText());

        });
    }

    public void setMessage(String message) {
        this.lblError.setText(message);
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
        this.lastContent = content;
        txtCode.setText(content);
    }

    String getEditorContent() {
        return txtCode.getText();
    }

    abstract Node getRenderNode();

    abstract void clearRenderNode();

    abstract void onEditorChange(String newContent);

}
