package org.fxapps.llmfx.windows;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.animation.PauseTransition;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.Toolkit;

@Singleton
public class ScreenshotWindow {

    private Robot robot;
    private Consumer<WritableImage> callback;
    private Stage captureWindow;

    @PostConstruct
    void setup() {
        this.robot = new Robot();
        var dimensions = Toolkit.getDefaultToolkit().getScreenSize();

        var dragRect = new Rectangle();
        var captureRoot = new AnchorPane(dragRect);
        
        captureWindow = new Stage();
        captureWindow.initStyle(StageStyle.TRANSPARENT);
        dragRect.setVisible(false);
        captureRoot.setCursor(Cursor.CROSSHAIR);
        captureRoot.setPrefSize(dimensions.getWidth(), dimensions.getHeight());
        captureWindow.setScene(new Scene(captureRoot,
                dimensions.getWidth(),
                dimensions.getHeight(),
                Color.TRANSPARENT));
        var initX = new AtomicReference<Double>();
        var initY = new AtomicReference<Double>();

        captureWindow.setOnShown(e -> captureRoot.setOpacity(0.3));
        captureRoot.setOnMousePressed(e -> {
            initX.set(e.getScreenX());
            initY.set(e.getScreenY());
        });
        captureRoot.setOnMouseDragged(e -> {
            dragRect.setVisible(true);
            var xDiff = e.getScreenX() - initX.get();
            var yDiff = e.getScreenY() - initY.get();
            if (xDiff >= 0) {
                dragRect.setX(initX.get());
                dragRect.setWidth(xDiff);
            } else {
                dragRect.setX(e.getScreenX());
                dragRect.setWidth(Math.abs(xDiff));
            }
            if (yDiff >= 0) {
                dragRect.setY(initY.get());
                dragRect.setHeight(yDiff);
            } else {
                dragRect.setY(e.getScreenY());
                dragRect.setHeight(Math.abs(yDiff));
            }

        });
        captureRoot.setOnMouseReleased(e -> {
            dragRect.setVisible(false);
            captureRoot.setOpacity(0.0);
            var pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(_e -> {
                var img = robot.getScreenCapture(null, dragRect.getX(), dragRect.getY(), dragRect.getWidth(),
                        dragRect.getHeight());
                if (callback != null) {
                    callback.accept(img);
                }
                captureWindow.hide();
            });
            pause.play();
        });

    }


    public void capture(Consumer<WritableImage> callback) {
        this.callback = callback;
        captureWindow.show();
    }   

}
