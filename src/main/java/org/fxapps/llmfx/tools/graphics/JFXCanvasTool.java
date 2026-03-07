package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

@Singleton
public class JFXCanvasTool implements JFXTool {

    private static final int CANVAS_WIDTH = 10000;
    private static final int CANVAS_HEIGHT = 10000;

    private GraphicsContext ctx;
    private ScrollPane root;

    @PostConstruct
    public void init() {
        var canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        this.ctx = canvas.getGraphicsContext2D();
        this.root = new ScrollPane(canvas);
    }

    public void clear() {
        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    public Node getRoot() {
        return root;
    }

    @Tool("Clears the rect area, deleting all the drawn objects on that area")
    void clearRect(double x, double y, double width, double height) {
        ctx.clearRect(x, y, width, height);
    }

    @Tool("""
            Set the color for shapes and lines stroke.
            Call this to set the stroke color of anything you are drawing.
            Make sure to call this before drawing something
            """)
    void setStrokeColor(@P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
    }

    @Tool("""
            Set the color for shapes and lines fill.
            Call this to set the fill color of anything you are drawing.
            Make sure to call this before drawing something
            """)
    void setColor(@P("Color in web format") String color) {
        ctx.setFill(fixColor(color));
    }

    @Tool("Draws a Image at the specific x and y location")
    public void drawImage(
            @P("The image URL. Can be a web URL or a base64 valid URL") String url,
            double x,
            double y,
            double width,
            double height) {
        var img = new Image(url);
        ctx.drawImage(img, x, y, width, height);
    }

    @Tool("""
            Sets a radial gradient as the color.
            """)
    public void setRadialGradient(
            @P("the angle in degrees from the center of the gradient to the focus point to which the first color is mapped") double focusAngle,
            @P("the distance from the center of the gradient to the focus point to which the first color is mapped") double focusDistance,
            @P("X coordinate of the center point of the gradient's circle. Values are from 0.0 to 1.0") double centerX,
            @P("Y coordinate of the center point of the gradient's circle. Values are from 0.0 to 1.0") double centerY,
            @P("the radius of the circle defining the extents of the color gradient") double radius,
            String[] colorStops) {
        var stops = new Stop[colorStops.length];
        var offsetStep = 1.0 / colorStops.length;
        for (int i = 0; i < colorStops.length; i++) {
            stops[i] = new Stop(i * offsetStep, fixColor(colorStops[i]));
        }
        var radialGradient = new RadialGradient(
                focusAngle,
                focusDistance,
                centerX,
                centerY,
                radius,
                true,
                CycleMethod.REPEAT,
                stops);
        ctx.setFill(radialGradient);
    }

    @Tool("""
            Sets a linear gradient as the color.
            """)
    void setLinearGradient(
            @P("X coordinate of the gradient axis start point. Values are from 0.0 to 1.0") double startX,
            @P(" Y coordinate of the gradient axis start point. Values are from 0.0 to 1.0") double startY,
            @P("X coordinate of the gradient axis end point. Values are from 0.0 to 1.0") double endX,
            @P("Y coordinate of the gradient axis end point. Values are from 0.0 to 1.0") double endY,
            String[] colorStops) {
        var stops = new Stop[colorStops.length];
        var offsetStep = 1.0 / colorStops.length;
        for (int i = 0; i < colorStops.length; i++) {
            stops[i] = new Stop(i * offsetStep, fixColor(colorStops[i]));
        }
        var fill = new LinearGradient(
                startX,
                startY,
                endX,
                endY,
                true,
                CycleMethod.REPEAT,
                stops);
        ctx.setFill(fill);
    }

    @Tool("Draws a circle")
    void drawCircle(
            double x,
            double y,
            double radius) {
        ctx.strokeOval(x, y, radius, radius);
        ctx.fillOval(x, y, radius, radius);
    }

    @Tool("Draw an oval")
    void drawOval(double x, double y, double width, double height) {
        ctx.strokeOval(x, y, width, height);
        ctx.fillOval(x, y, width, height);
    }

    @Tool("Draws a polygon with the given points using the provided parameters")
    void drawPolygon(double[] xPoints, double[] yPoints, int nPoints) {
        ctx.strokePolygon(xPoints, yPoints, nPoints);
        ctx.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Tool("Draws a rectangle")
    void drawRect(
            @P("top-left corner X") double x,
            @P("top-left corner Y") double y,
            @P("Rectangle width") double width,
            @P("Rectangle height") double height) {
        ctx.strokeRect(x, y, width, height);
        ctx.fillRect(x, y, width, height);
    }

    @Tool("""
            Draws a rounded rectangle using the provided parameters
            """)
    void drawRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        ctx.strokeRoundRect(x, y, width, height, arcWidth, arcHeight);
        ctx.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Tool("""
            Draws the the given text at position x, y.
            To change the font settings you must call setFont before.

            """)
    void drawText(String text, double x, double y) {
        ctx.strokeText(text, x, y);
        ctx.fillText(text, x, y);
    }

    @Tool("""
            Sets the Font.
            """)
    void setFont(String family, FontWeight weight, FontPosture posture, double size) {
        ctx.setFont(Font.font(family, weight, posture, size));
    }

    @Tool("""
            Sets the Font Smoothing Type.
            """)
    void setFontSmoothingType(FontSmoothingType fontsmoothing) {
        ctx.setFontSmoothingType(fontsmoothing);
    }

    @Tool("""
            Sets the stroke line cap.
            """)
    void setLineCap(StrokeLineCap cap) {
        ctx.setLineCap(cap);

    }

    @Tool("""
            Sets the line width.
            """)
    void setLineWidth(double lineWidth) {
        ctx.setLineWidth(lineWidth);
    }

    @Tool("""
            Sets the miter limit.
            """)
    void setMiterLimit(double ml) {
        ctx.setMiterLimit(ml);
    }

    @Tool("""
            Defines horizontal text alignment, relative to the text x origin.
            Call this before drawing text
            """)
    void setTextAlign(TextAlignment align) {
        ctx.setTextAlign(align);
    }

    @Tool("""
            Sets the current Text Baseline.
            """)
    void setTextBaseline(VPos baseline) {
        ctx.setTextBaseline(baseline);
    }

    @Tool("""
            Draws a line between two points on the canvas.
            """)
    void drawLine(@P("starting point X") double x1,
            @P("starting point Y") double y1,
            @P("ending point X") double x2,
            @P("ending point Y") double y2) {
        ctx.strokeLine(x1, y1, x2, y2);
    }

    @Tool("""
            Sets the line dash for strokes forr all shapes and lines.
            If you want to reset a dash call this with a null or empty array.
            """)
    void setLineDashes(
            @P("the array of finite non-negative dash lengths. Use an empty array or null to clear this configuration") double[] dashes) {
        ctx.setLineDashes(dashes);
    }

    // complex path tools

    @Tool("Reset the current Path and starts a new on. Use the path tools to continue this path")
    public void beginPath() {
        ctx.beginPath();
    }

    @Tool("Moves the path to the specified x, y coordinate")
    public void moveTo(double x, double y) {
        ctx.moveTo(x, y);
    }

    @Tool("Adds segments to the current path to make a line to the given x,y coordinate")
    public void lineTo(double x, double y) {
        ctx.lineTo(x, y);
    }

    @Tool("Adds segments to the current path to make a quadratic Bezier curve.")
    public void quadraticCurveTo(
            @P("X coordinate of the control point") double xc,
            @P("Y coordinate of the control point") double yc,
            @P("X coordinate of the end point") double x1,
            @P("Y coordinate of the end point") double y1) {
        ctx.quadraticCurveTo(xc, yc, x1, y1);
    }

    @Tool("Add a bezier curve to the current path")
    public void bezierCurveTo(
            @P("X coordinate of first Bezier control point.") int xc1,
            @P("Y coordinate of first Bezier control point.") int yc1,
            @P("X coordinate of the second Bezier control point.") int xc2,
            @P("Y coordinate of the second Bezier control point.") int yc2,
            @P("X coordinate of the end point.") int x,
            @P("Y coordinate of the end point.") int y) {
        ctx.bezierCurveTo(xc1, yc1, xc2, yc2, x, y);
    }

    @Path("Adds path elements to the current path to make an arc that uses Euclidean degrees")
    public void arc(
            double centerX,
            double centerY,
            double radiusX,
            double radiusY,
            double startAngle,
            double length) {
        ctx.arc(centerX, centerY, radiusX, radiusY, startAngle, length);
    }

    @Path("Adds segments to the current path to make an arc. ")
    public void arcTo(
            double x1,
            double y1,
            double x2,
            double y2,
            double radius) {
        ctx.arcTo(x1, y1, x2, y2, radius);
    }

    @Path("Closes the path.")
    public void closePath() {
        ctx.closePath();
    }

    @Path("Adds path elements to the current path to make a rectangle.")
    public void rect(
            double x,
            double y,
            double width,
            double height) {
        ctx.rect(x, y, width, height);
    }

    @Path("Fills the path with the current fill paint.")
    public void fill() {
        ctx.fill();
    }

    @Path("Strokes the path with the current stroke paint.")
    public void stroke() {
        ctx.stroke();
    }

    @Tool("Append a SVG path to the current path")
    public void appendSVGPath(String svgPath) {
        ctx.appendSVGPath(svgPath);
        ctx.stroke();
        ctx.fill();
    }
}
