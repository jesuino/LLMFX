package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

// TODO: Pehraps let the LLM give an ID is a good idea, it keeps making the wrong ids for the shapes
@Singleton
public class JFXShapesTool {

        private Group container;

        private final AtomicInteger SHAPE_ID_CONTROL = new AtomicInteger();
        private final AtomicInteger GROUP_ID_CONTROL = new AtomicInteger();

        public void setContainer(Group container) {
                this.container = container;

        }

        @Tool("Creates an arc")
        public String arc(
                        @P("X coordinate of the center point of the arc.") double centerX,
                        @P("Y coordinate of the center point of the arc.") double centerY,
                        @P("angular extent of the arc in degrees.") double lengh,
                        @P("overall width (horizontal radius) of the full ellipse of which this arc is a partial section.") double radiusX,
                        @P("overall height (vertical radius) of the full ellipse of which this arc is a partial section.") double radiusY,
                        @P("starting angle of the arc in degrees.") double startAngle,
                        @P("closure type for the arc: OPEN, CHORD,or ROUND.") ArcType arcType,
                        String color,
                        String strokeColor,
                        double strokeWidth) {

                var arc = new Arc(centerX, centerY, radiusX, radiusY, startAngle, lengh);
                return setupShape(arc, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a circle")
        public String circle(
                        @P("horizontal position of the center of the circle in pixels.") double centerX,
                        @P("vertical position of the center of the circle in pixels.") double centerY,
                        @P("radius of the circle in pixels.") double radius,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var circle = new Circle(centerX, centerY, radius);
                return setupShape(circle, color, strokeColor, strokeWidth);

        }

        @Tool("Create an ellipse and return its ID, use it it for effects and animations")
        public String ellipse(
                        @P("horizontal position of the center of the ellipse in pixels.") double centerX,
                        @P("vertical position of the center of the ellipse in pixels.") double centerY,
                        @P("width of the ellipse in pixels.") double radiusX,
                        @P("height of the ellipse in pixels.") double radiusY,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var ellipse = new Ellipse(centerX, centerY, radiusX, radiusY);
                return setupShape(ellipse, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a line")
        public String line(
                        @P("X-coordinate of the starting point") double startX,
                        @P("Y-coordinate of the starting point") double startY,
                        @P("X-coordinate of the ending point") double endX,
                        @P("Y-coordinate of the ending point") double endY,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var line = new Line(startX, startY, endX, endY);
                return setupShape(line, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a polygon")
        public String polygon(
                        @P("coordinates of the polygon vertices") double[] points,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var polygon = new Polygon(points);
                return setupShape(polygon, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a polyline")
        public String polyline(
                        @P("coordinates of the polyline vertices") double[] points,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var polyline = new Polyline(points);
                return setupShape(polyline, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a rectangle")
        public String rectangle(
                        @P("X coordinate of the upper-left corner of the rectangle.") double x,
                        @P("Y coordinate of the upper-left corner of the rectangle.") double y,
                        double width,
                        double height,
                        @P("horizontal diameter of the arc at the four corners of the rectangle.") double arcWidth,
                        @P("vertical diameter of the arc at the four corners of the rectangle.") double arcHeight,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var rectangle = new Rectangle(x, y, width, height);
                rectangle.setArcWidth(arcWidth);
                rectangle.setArcHeight(arcHeight);
                return setupShape(rectangle, color, strokeColor, strokeWidth);
        }

        @Tool("Creates an arc")
        public String svgPath(
                        @P("SVG Path") String content,
                        @P("filling rule constant for determining the interior of the path.") FillRule fillRule,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var svgPath = new SVGPath();
                svgPath.setContent(content);
                svgPath.setFillRule(fillRule);
                return setupShape(svgPath, color, strokeColor, strokeWidth);
        }

        @Tool("Creates a text")
        public String text(
                        String text,
                        @P("X coordinate of text origin.") double x,
                        @P("Y coordinate of text origin.") double y,
                        String fontFamily,
                        FontWeight fontWeight,
                        FontPosture fontPosture,
                        int fontSize,
                        TextAlignment alignment,
                        double wrappingWidth,
                        VPos textOrigin,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var font = Font.font(fontFamily, fontWeight, fontPosture, fontSize);
                var txt = new Text(text);
                txt.setFont(font);
                txt.setTextAlignment(alignment);
                txt.setWrappingWidth(wrappingWidth);
                txt.setTextOrigin(textOrigin);
                return setupShape(txt, color, strokeColor, strokeWidth);
        }

        @Tool("Defines a quadratic Bézier parametric curve segment in (x,y) coordinate space")
        public String quadCurve(
                        @P("X coordinate of the start point of the quadratic curve segment.") double startX,
                        @P("Y coordinate of the start point of the quadratic curve segment.") double startY,
                        @P("X coordinate of the control point of the quadratic curve segment") double controlX,
                        @P("Y coordinate of the control point of the quadratic curve segment") double controlY,
                        @P("X coordinate of the end point of the quadratic curve segment.") double endX,
                        @P("Y coordinate of the end point of the quadratic curve segment.") double endY,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var quadCurve = new QuadCurve(startX, startY, controlX, controlY, endX, endY);
                return setupShape(quadCurve, color, strokeColor, strokeWidth);
        }

        @Tool("Defines a cubic Bézier parametric curve segment in (x,y) coordinate space")
        public String cubicCurve(
                        @P("X coordinate of the start point of the quadratic curve segment.") double startX,
                        @P("Y coordinate of the start point of the quadratic curve segment.") double startY,
                        @P("X coordinate of the first control point of the quadratic curve segment") double controlX1,
                        @P("Y coordinate of the first control point of the quadratic curve segment") double controlY1,
                        @P("X coordinate of the second control point of the quadratic curve segment") double controlX2,
                        @P("Y coordinate of the second control point of the quadratic curve segment") double controlY2,
                        @P("X coordinate of the end point of the quadratic curve segment.") double endX,
                        @P("Y coordinate of the end point of the quadratic curve segment.") double endY,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                var cubicCurve = new CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
                return setupShape(cubicCurve, color, strokeColor, strokeWidth);
        }

        @Tool("Group the shapes")
        public String groupShapes(@P("The list of shape ids to be grouped") String... shapeIds) {
                var grp = new Group();
                var nodes = Arrays.stream(shapeIds)
                                .map(this::findNodeById)
                                .toList();
                grp.setId("group" + GROUP_ID_CONTROL.incrementAndGet());
                Platform.runLater(() -> {
                        nodes.forEach(container.getChildren()::remove);
                        grp.getChildren().addAll(nodes);
                        container.getChildren().add(grp);

                });
                return grp.getId();
        }

        @Tool("Return all shapes and groups IDs")
        public List<String> listAllShapes() {
                return container.getChildren()
                                .stream().map(Node::getId).toList();
        }

        private Node findNodeById(String id) {
                return container.getChildren()
                                .stream()
                                .filter(n -> id.equals(n.getId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Shape with ID " + id + " not found"));
        }

        // TRANSITIONS - Could this be a separated tool?
        // ALSO, should we keep track of the transitions to allow the LLM to control it?

        @Tool("Animation of the X and Y position of an item")
        public void animateShapePosition(
                        @P("item to be animated") String itemId,
                        @P("target X position or null to animate only Y position") Double x,
                        @P("target Y position or null to animate only X position") Double y,
                        @P("animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                if (x == null && y == null) {
                        throw new IllegalArgumentException("You need to provide either X or Y position");
                }
                var node = findNodeById(itemId);
                var translateTransition = new TranslateTransition(Duration.millis(duration), node);
                if (x != null)
                        translateTransition.setByX(x);
                if (y != null)
                        translateTransition.setByY(y);
                setupTransition(translateTransition, autoReverse, cycles);
        }

        @Tool("Animation of the X and Y scale of a given shape or group")
        public void animateShapeSize(
                        @P("The item to be animated") String itemId,
                        @P("The target X scale. For example, if the value is 1.1, then the X scale will increase by 10%. Use null to animate only Y") Double x,
                        @P("The target Y scape. For example, if the value is 1.1, then the Y scale will increase by 10%. Use null to animate only X") Double y,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles.") int cycles) {

                if (x == null && y == null) {
                        throw new IllegalArgumentException("You need to provide either X or Y position");
                }
                var node = findNodeById(itemId);
                var scale = new ScaleTransition(Duration.millis(duration), node);
                if (x != null)
                        scale.setByX(x);

                if (y != null)
                        scale.setByY(y);
                setupTransition(scale, autoReverse, cycles);
        }

        @Tool("Animate the opacity of an item")
        public void animateShapeOpacity(
                        @P("item to be animated") String itemId,
                        @P("animation starting opacity") double startOpacity,
                        @P("animation end opacity") double endOpacity,
                        @P("animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("number of cycles. Use -1 for infinite cycles.") int cycles) {
                var node = findNodeById(itemId);
                var fade = new FadeTransition(Duration.millis(duration), node);
                fade.setFromValue(startOpacity);
                fade.setToValue(endOpacity);
                setupTransition(fade, autoReverse, cycles);
        }

        @Tool("Rotate an item")
        public void animateShapeRotate(
                        @P("The node to be animated") String nodeId,
                        @P("The target angle") double angle,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles.") int cycles) {
                var node = findNodeById(nodeId);
                var rotate = new RotateTransition(Duration.millis(duration), node);
                rotate.setByAngle(angle);
                setupTransition(rotate, autoReverse, cycles);
        }

        @Tool("Changes the color of a Shape in a given interval")
        public void animateShapeColor(
                        @P("The shape to be animated") String shapeId,
                        @P("The color to start the transtion. Use null if you want to transition from the shape current color") String fromColor,
                        @P("The target color") String toColor,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles.") int cycles) {
                var node = findNodeById(shapeId);
                if (node instanceof Shape shape) {
                        var fill = new FillTransition(Duration.millis(duration), shape);
                        var _fromColor = fromColor == null ? (Color) shape.getFill() : fixColor(fromColor);
                        fill.setFromValue(_fromColor);
                        fill.setToValue(fixColor(toColor));
                        setupTransition(fill, autoReverse, cycles);

                } else {
                        throw new IllegalArgumentException("Node is not a shape!");
                }
        }

        private void setupTransition(Transition animation, boolean autoReverse, int cycles) {
                animation.setCycleCount(cycles);
                animation.setAutoReverse(autoReverse);
                animation.play();
        }

        private String setupShape(
                        Shape shape,
                        String color,
                        String strokeColor,
                        double strokeWidth) {
                shape.setFill(fixColor(color));
                shape.setStroke(fixColor(color));
                shape.setStrokeWidth(strokeWidth);
                shape.setId(shape.getClass().getSimpleName().toLowerCase() + SHAPE_ID_CONTROL.incrementAndGet());
                Platform.runLater(() -> container.getChildren().add(shape));
                return "ID: " + shape.getId();
        }

}
