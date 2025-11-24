package org.fxapps.llmfx.tools.graphics;

import java.util.stream.IntStream;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;

@Singleton
public class JFXDrawerTool extends EditorJFXTool {

    static final int CANVAS_WIDTH = 1200;
    static final int CANVAS_HEIGHT = 900;

    private Canvas canvas;

    GraphicsContext gc;
    private ScrollPane root;

    @PostConstruct
    public void init() {
        this.canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.root = new ScrollPane(canvas);
        super.init();
    }

    @Override
    void onEditorChange(String newContent) {
        this.draw(newContent);
    }

    @Override
    Node getRenderNode() {
        return root;
    }

    @Override
    void clearRenderNode() {
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    @Tool("""
            You can draw using the following commands separated by line break (\n):
            background r g b
            rect x y w h
            fillRect x y w h
            circle cx cy r
            fillCircle cx cy r
            ellipse cx cy rw rh
            fillEllipse cx cy rw rh
            line x1 y1 x2 y2
            polyline x1 y1 x2 y2 … xn yn
            polygon x1 y1 x2 y2 … xn yn
            fillPolygon x1 y1 x2 y2 … xn yn
            text x y text
            color r g b
            width w
            text x y "string"
            """)
    public void draw(String dsl) {
        applyDSL(dsl);
        setEditorContent(dsl);
    }

    void applyDSL(String dsl) {
        clearRenderNode();
        String[] lines = dsl.split("\\R");
        for (String line : lines) {
            var tokens = line.trim().split("\\s+");
            if (tokens.length == 0)
                continue;
            var command = tokens[0];
            var text = "";
            if (command.equals("text")) {
                text = tokens[tokens.length - 1].replaceAll("^\"|\"$", "");
                tokens[tokens.length - 1] = null;
            }
            double[] params = IntStream.range(1, tokens.length)
                    .mapToObj(i -> tokens[i])
                    .filter(v -> v != null && !v.isBlank())
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            switch (command) {
                case "rect" -> gc.strokeRect(params[0], params[1], params[2], params[3]);
                case "circle" -> gc.strokeOval(params[0], params[1], params[2], params[2]);
                case "fillRect" -> gc.fillRect(params[0], params[1], params[2], params[3]);
                case "fillCircle" -> gc.fillOval(params[0], params[1], params[2], params[2]);
                case "ellipse" -> gc.strokeOval(params[0], params[1], params[2], params[3]);
                case "fillEllipse" -> gc.fillOval(params[0], params[1], params[2], params[3]);
                case "line" -> gc.strokeLine(params[0], params[1], params[2], params[3]);
                case "polyline" -> {
                    var points = extractPoints(params);
                    gc.strokePolyline(points[0], points[1], points[0].length);
                }
                case "polygon" -> {
                    var points = extractPoints(params);
                    gc.strokePolygon(points[0], points[1], points[0].length);
                }
                case "fillPolygon" -> {
                    var points = extractPoints(params);
                    gc.fillPolygon(points[0], points[1], points[0].length);
                }
                case "width" -> gc.setLineWidth(params[0]);
                case "text" -> gc.fillText(text, params[0], params[1]);
                case "background" -> {
                    var fill = gc.getFill();
                    gc.setFill(Color.rgb((int) params[0], (int) params[1], (int) params[2]));
                    gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                    gc.setFill(fill);
                }
                case "color" -> {
                    var color = Color.rgb((int) params[0], (int) params[1], (int) params[2]);
                    gc.setStroke(color);
                    gc.setFill(color);
                }

            }
        }
    }

    private double[][] extractPoints(double[] params) {
        double[] xPoints = new double[params.length / 2];
        double[] yPoints = new double[params.length / 2];
        for (int i = 0; i < params.length; i += 2) {
            xPoints[i / 2] = params[i];
            yPoints[i / 2] = params[i + 1];
        }
        return new double[][] { xPoints, yPoints };
    }

}
