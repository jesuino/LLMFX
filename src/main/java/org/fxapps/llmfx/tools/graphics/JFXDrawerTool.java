package org.fxapps.llmfx.tools.graphics;

import org.fxapps.llmfx.Events.ClearDrawingEvent;
import org.fxapps.llmfx.Events.NewDrawingNodeEvent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

@Singleton
public class JFXDrawerTool {

    @Inject
    Event<NewDrawingNodeEvent> newDrawingNodeEvent;

    @Inject
    Event<ClearDrawingEvent> clearDrawingEvent;


    @Tool("Draws a text. You can draw any text provided by the user. The text will be drawn at the specified coordinates.")
    public void text(
            @P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("The text to be drawn") String text,
            @P("Font size") int fontSize,
            @P("Font color in web format") String fontColor,
            @P("Stroke color in web format") String strokeColor) {
        Text textNode = new Text(x, y, text);
        textNode.setTranslateX(x);
        textNode.setTranslateY(y);
        textNode.setFont(Font.font(fontSize));
        applyShapeColor(textNode, fontColor, strokeColor);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(textNode));
    }

    @Tool("Draws a rectangle. You can draw a rectangle at the specified coordinates with the specified width, height and color")
    public void rectangle(
            @P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("The rectangle width") int width,
            @P("The rectangle height") int height,
            @P("Defines the horizontal diameter of the arc at the four corners of the rectangle.") int arcWidth,
            @P("Defines the vertical diameter of the arc at the four corners of the rectangle.") int arcHeight,
            @P("Color in web format") String color,
            @P("Stroke color in web format") String strokeColor) {
        javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(width, height);
        rectangle.setTranslateX(x);
        rectangle.setTranslateY(y);
        rectangle.setArcWidth(arcWidth);
        rectangle.setArcHeight(arcHeight);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        applyShapeColor(rectangle, color, strokeColor);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(rectangle));
    }

    @Tool("""
            Draws an ellipse or a circle.
            You can draw an ellipse at the specified coordinates with the specified width, height and color.
             If radiusX and radiusY are the same, it will be a circle.
            """)
    public void ellipse(
            @P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("The ellipse X radius") int radiusX,
            @P("The ellipse Y radius") int radiusY,
            @P("Color in web format") String color,
            @P("Stroke color in web format") String strokeColor) {
        Ellipse ellipse = new Ellipse();
        ellipse.setTranslateX(x);
        ellipse.setTranslateY(y);
        ellipse.setRadiusX(radiusX);
        ellipse.setRadiusY(radiusY);
        applyShapeColor(ellipse, color, color);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(ellipse));
    }

    @Tool("Draws a polygon. You can draw a polygon at the specified x and Y and with the specified coordinates and color")
    public void polygon(@P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("A list of x,y coordinates of the Polygon vertices") Double[] coordinates,
            @P("Color in web format") String color,
            @P("Stroke color in web format") String strokeColor) {
        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(coordinates);
        polygon.setTranslateX(x);
        polygon.setTranslateY(y);
        applyShapeColor(polygon, color, strokeColor);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(polygon));
    }

    @Tool("""
            Draws an Arc.
            The Arc class represents a 2D arc object, defined by a center point, start angle (in degrees), angular extent (length of the arc in degrees), and an arc type
            """)
    public void arc(@P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("The arc X radius") int radiusX,
            @P("The arc Y radius") int radiusY,
            @P("Defines the starting angle of the arc in degrees.") float startAngle,
            @P("Defines the angular extent of the arc in degrees.") float length,
            @P("Defines the closure type for the arc. The possible values are ROUND, CHORD or OPEN") String arcType,
            @P("Color in web format") String color,
            @P("Stroke color in web format") String strokeColor) {
        Arc arc = new Arc();
        arc.setTranslateX(x);
        arc.setTranslateY(y);
        arc.setRadiusX(radiusX);
        arc.setRadiusY(radiusY);
        arc.setStartAngle(startAngle);
        arc.setLength(length);
        arc.setType(ArcType.valueOf(arcType));
        applyShapeColor(arc, color, strokeColor);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(arc));
    }

    @Tool("Draws a line. You can draw a line at the specified coordinates with the specified color")
    public void line(
            @P("Start position X") double startX,
            @P("Start position Y") double startY,
            @P("End position X") double endX,
            @P("End position Y") double endY,
            @P("The line stroke width") double strokeWidth,
            @P("Color in web format") String color) {
        Line line = new Line();
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        line.setStrokeWidth(strokeWidth);
        applyShapeColor(line, color, color);
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(line));
    }

   

    private void applyShapeColor(Shape shape, String color, String strokeColor) {
        shape.setFill(Color.valueOf(color));
        shape.setStroke(Color.valueOf(strokeColor));
    }

}
