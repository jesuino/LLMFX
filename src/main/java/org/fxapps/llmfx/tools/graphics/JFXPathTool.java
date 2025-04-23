package org.fxapps.llmfx.tools.graphics;

import org.fxapps.llmfx.Events.ClearDrawingEvent;
import org.fxapps.llmfx.Events.NewDrawingNodeEvent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

@Singleton
public class JFXPathTool {

    @Inject
    Event<NewDrawingNodeEvent> newDrawingNodeEvent;

    @Inject
    Event<ClearDrawingEvent> clearDrawingEvent;

    private Path currentPath;

    @Tool("""
            Initiates the path for drawing.
            You can use the other tools lineTo, moveTo, arcTo,  hlineTo, vlineTo and quadCurveTo to add elements to this path.
            Remember to call finishPath when you are done.
            """)
    public void initPath(
            @P("The x position on the screen") int x,
            @P("The y position on the screen") int y,
            @P("Fill color in web format") String fillColor,
            @P("Path stroke width") double strokeWidth,
            @P("Stroke color in web format") String strokeColor) {
        this.currentPath = new Path();
        this.currentPath.setFillRule(FillRule.EVEN_ODD);
        this.currentPath.setTranslateX(x);
        this.currentPath.setTranslateY(y);
        this.currentPath.setStrokeWidth(strokeWidth);
        this.currentPath.setStroke(correctColor(strokeColor));
        this.currentPath.setFill(correctColor(fillColor));
        this.currentPath.getElements().add(new MoveTo(0, 0));
    }

    @Tool("""
            Adds a line to the path. Remember to call initPath before calling this tool.
                """)
    public void lineTo(
            @P("The line end X position") int x,
            @P("The line end Y position") int y) {
        checkIfPathIsInitialized();
        LineTo lineTo = new LineTo(x, y);
        this.currentPath.getElements().add(lineTo);
    }

    @Tool("""
            Adds an arc to the path. Remember to call initPath before calling this tool.
                """)
    public void arcTo(
            @P("The x coordinate to arc to.") double x,
            @P("The y coordinate to arc to.") double y,
            @P("The horizontal radius to use for the arc.") double radiusX,
            @P("The vertical radius to use for the arc.") double radiusY,
            @P("The x-axis rotation in degrees.") double xAxisRotation,
            @P("The large arc flag.") boolean largeArcFlag,
            @P("The sweep flag.") boolean sweepFlag

    ) {
        checkIfPathIsInitialized();
        var arcTo = new ArcTo(radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
        this.currentPath.getElements().add(arcTo);
    }

    @Tool("""
            Adds a quad curve to the path. Remember to call initPath before calling this tool.
                """)
    public void quadCurveTo(
            @P("Defines the X coordinate of the final end point.") double x,
            @P("Defines the Y coordinate of the final end point.") double y,
            @P("Defines the X coordinate of the quadratic control point.") double controlX,
            @P("Defines the Y coordinate of the quadratic control point.") double controlY) {
        checkIfPathIsInitialized();
        var quadCurveTo = new QuadCurveTo(controlX, controlY, x, y);
        this.currentPath.getElements().add(quadCurveTo);
    }

    @Tool("""
            Moves the path to a new position. Remember to call initPath before calling this tool.
                """)
    public void moveTo(
            @P("The x position to move to") int x,
            @P("The y position to move to") int y) {
        checkIfPathIsInitialized();
        this.currentPath.getElements().add(new MoveTo(x, y));
    }

    @Tool("""
            Adds a cubic curve to the path. Remember to call initPath before calling this tool.
                """)
    public void cubicCurveTo(
            @P("Defines the X coordinate of the final end point.") double x,
            @P("Defines the Y coordinate of the final end point.") double y,
            @P("Defines the X coordinate of the first control point.") double controlX1,
            @P("Defines the Y coordinate of the first control point.") double controlY1,
            @P("Defines the X coordinate of the second control point.") double controlX2,
            @P("Defines the Y coordinate of the second control point.") double controlY2) {
        checkIfPathIsInitialized();
        var cubicCurveTo = new CubicCurveTo(controlX1, controlY1, controlX2, controlY2, x, y);
        this.currentPath.getElements().add(cubicCurveTo);
    }

    @Tool("""
            Adds a horizontal line to the path. Remember to call initPath before calling this tool.
                """)
    public void hlineTo(
            @P("The x position to draw the line to") int x) {
        checkIfPathIsInitialized();
        this.currentPath.getElements().add(new HLineTo(x));
    }

    @Tool("""
            Adds a vertical line to the path. Remember to call initPath before calling this tool.
                """)
    public void vlineTo(
            @P("The y position to draw the line to") int y) {
        checkIfPathIsInitialized();
        this.currentPath.getElements().add(new javafx.scene.shape.VLineTo(y));
    }

    @Tool("""
            Closes the path connecting the end to the beginning.
            Remember to call initPath before calling this tool.
                """)
    public void closePath() {
        checkIfPathIsInitialized();
        this.currentPath.getElements().add(new ClosePath());
    }

    @Tool("""
            Finish the current path. Remember to call initPath before calling this tool.
                """)
    public void finishPath() {
        checkIfPathIsInitialized();
        this.currentPath.getElements().add(new ClosePath());
        newDrawingNodeEvent.fire(new NewDrawingNodeEvent(this.currentPath));
        this.currentPath = null;
    }

    @Tool("Clear the canvas.")
    public void clear() {
        clearDrawingEvent.fire(new ClearDrawingEvent());
    }

    private void checkIfPathIsInitialized() {
        if (this.currentPath == null) {
            throw new IllegalStateException("You must call initPath before calling this tools");
        }
    }

    private Color correctColor(String color) {
        try {
            return Color.valueOf(color);
        } catch (Exception e) {
            System.out.println("Invalid color format: " + color);
            return Color.BLACK;
        }
    }

}
