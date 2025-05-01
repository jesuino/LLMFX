package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

@Singleton
public class JFXCanvasTool {

    private GraphicsContext ctx;

    public void setContext(GraphicsContext ctx) {
        this.ctx = ctx;
    }

    @Tool("""
            Clears the rect area, deleting all the drawn objects on that area
            """)
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

    @Tool("""
            Set a gradient as the color.
            """)
    void setGradient(

            @P("the X coordinate of the gradient axis start point. Values are from 0.0 to 1.0") double startX,
            @P(" the Y coordinate of the gradient axis start point. Values are from 0.0 to 1.0") double startY,
            @P("the X coordinate of the gradient axis end point. Values are from 0.0 to 1.0") double endX,
            @P("the Y coordinate of the gradient axis end point. Values are from 0.0 to 1.0") double endY,

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

    @Tool("""
            Draw a circle at the specificed position
            """)
    void drawCircle(double x, double y, double radius) {
        ctx.strokeOval(x, y, y, radius);
        ctx.fillOval(x, y, radius, radius);
    }

    @Tool("""
            Draw an oval with the provided parameters
            """)
    void drawOval(double x, double y, double width, double height) {
        ctx.strokeOval(x, y, width, height);
        ctx.fillOval(x, y, width, height);
    }

    @Tool("""
            Draws a polygon with the given points using the provided parameters
            """)
    void drawPolygon(double[] xPoints, double[] yPoints, int nPoints) {
        ctx.strokePolygon(xPoints, yPoints, nPoints);
        ctx.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Draws a rectangle using the provided parameters
            """)
    void drawRect(double x, double y, double width, double height) {
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
            Draws the the given string of text at position x, y.
            To change the font settings you must call setFont before.

            """)
    void drawText(String text, double x, double y) {
        ctx.strokeText(text, x, y);
        ctx.fillText(text, x, y);
    }

    @Tool("""
            Set the filling rule attribute for determining the interior of paths in fill or clip operations.
            """)
    void setFillRule(FillRule fillRule) {
        ctx.setFillRule(fillRule);
    }

    @Tool("""
            Sets the current Font.
            """)
    void setFont(String family, FontWeight weight, FontPosture posture, double size) {
        ctx.setFont(Font.font(family, weight, posture, size));
    }

    @Tool("""
            Sets the current Font Smoothing Type.
            """)
    void setFontSmoothingType(FontSmoothingType fontsmoothing) {
        ctx.setFontSmoothingType(fontsmoothing);
    }

    @Tool("""
            Sets the current stroke line cap.
            """)
    void setLineCap(StrokeLineCap cap) {
        ctx.setLineCap(cap);

    }

    @Tool("""
            Sets the current line width.
            """)
    void setLineWidth(double lineWidth) {
        ctx.setLineWidth(lineWidth);
    }

    @Tool("""
            Sets the current miter limit.
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
            Strokes an Arc using the current stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeArc(double x, double y, double width, double height, double startAngle, double arcExtent,
            ArcType closure, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeArc(x, y, width, height, startAngle, arcExtent, closure);
    }

    @Tool("""
            Strokes a line using the current stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeLine(double x1, double y1, double x2, double y2, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeLine(x1, y1, x2, y2);
    }

    @Tool("""
            Strokes an oval using the current stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeOval(double x, double y, double width, double height, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeOval(x, y, width, height);
    }

    @Tool("""
            Strokes a polygon with the given points using the currently set stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokePolygon(double[] xPoints, double[] yPoints, int nPoints, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokePolygon(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Strokes a polyline with the given points using the currently set stroke paint attribute.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokePolyline(double[] xPoints, double[] yPoints, int nPoints, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokePolyline(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Strokes a rectangle using the current stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeRect(double x, double y, double width, double height, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeRect(x, y, width, height);
    }

    @Tool("""
            Strokes a rounded rectangle using the current stroke paint.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight,
            @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Tool("""
            Draws text with stroke paint and includes a maximum width of the string.
            This tool draws only the stroke of the shape, with no fill
            """)
    void strokeText(String text, double x, double y, double maxWidth, @P("Color in web format") String color) {
        ctx.setStroke(fixColor(color));
        ctx.strokeText(text, x, y, maxWidth);
    }

    @Tool("""
            Draws a line using the current stroke paint.
            This tool draw only the stroke of the shape, with no fill
            """)
    void drawLine(double x1, double y1, double x2, double y2) {
        ctx.strokeLine(x1, y1, x2, y2);
    }

}
