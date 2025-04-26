package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import org.fxapps.llmfx.Events;
import org.fxapps.llmfx.Events.DrawingStartedEvent;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

@Singleton
public class JFXCanvasTool {


    private GraphicsContext ctx;

    @Inject
    Event<DrawingStartedEvent> drawingStartedEvent;

    public void setContext(GraphicsContext ctx) {
        this.ctx = ctx;
    }

    @Tool("""
        Call this to inform user that you started drawing
        """)
        void startedDrawing() {
            drawingStartedEvent.fire(new DrawingStartedEvent());
            
        }

    @Tool("""
            Fills an oval using the current fill paint.
            """)
    void fillOval(double x, double y, double width, double height) {
        ctx.fillOval(x, y, width, height);
    }

    @Tool("""
            Fills a polygon with the given points using the currently set fill paint.

            """)
    void fillPolygon(double[] xPoints, double[] yPoints, int nPoints) {
        ctx.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Fills a rectangle using the current fill paint.

            """)
    void fillRect(double x, double y, double width, double height) {
        ctx.fillRect(x, y, width, height);
    }

    @Tool("""
            Fills a rounded rectangle using the current fill paint.
            """)
    void fillRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        ctx.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Tool("""
            Fills the given string of text at position x, y with the current fill paint attribute.
            """)
    void fillText(String text, double x, double y) {
        ctx.fillText(text, x, y);
    }
    
    @Tool("""
                Sets the current fill paint attribute.
            """)
    void setFill(String color) {
        ctx.setFill(fixColor(color));
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
            Sets the current stroke paint attribute.
            """)
    void setStroke(String color) {
        ctx.setStroke(fixColor(color));
    }

    @Tool("""
            Defines horizontal text alignment, relative to the text x origin.
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
            """)
    void strokeArc(double x, double y, double width, double height, double startAngle, double arcExtent,
            ArcType closure) {
        ctx.strokeArc(x, y, width, height, startAngle, arcExtent, closure);
    }

    @Tool("""
            Strokes a line using the current stroke paint.
            """)
    void strokeLine(double x1, double y1, double x2, double y2) {
        ctx.strokeLine(x1, y1, x2, y2);
    }

    @Tool("""
            Strokes an oval using the current stroke paint.
            """)
    void strokeOval(double x, double y, double width, double height) {
        ctx.strokeOval(x, y, width, height);
    }

    @Tool("""
            Strokes a polygon with the given points using the currently set stroke paint.
            """)
    void strokePolygon(double[] xPoints, double[] yPoints, int nPoints) {
        ctx.strokePolygon(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Strokes a polyline with the given points using the currently set stroke paint attribute.
            """)
    void strokePolyline(double[] xPoints, double[] yPoints, int nPoints) {
        ctx.strokePolyline(xPoints, yPoints, nPoints);
    }

    @Tool("""
            Strokes a rectangle using the current stroke paint.
            """)
    void strokeRect(double x, double y, double width, double height) {
        ctx.strokeRect(x, y, width, height);
    }

    @Tool("""
            Strokes a rounded rectangle using the current stroke paint.
            """)
    void strokeRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        ctx.strokeRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Tool("""
            Draws text with stroke paint and includes a maximum width of the string.
            """)
    void strokeText(String text, double x, double y, double maxWidth) {
        ctx.strokeText(text, x, y, maxWidth);
    }
}
