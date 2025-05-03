package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.animation.Animation;
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

// TODO: Missing shapes:
// * CubicCurve, QuadCurve and Text
@Singleton
public class JFXShapesTool {

        private Group container;

        private final AtomicInteger SHAPE_ID_CONTROL = new AtomicInteger();
        private final AtomicInteger GROUP_ID_CONTROL = new AtomicInteger();

        public void setContainer(Group container) {
                this.container = container;

        }

        @Tool("Create an arc and return its ID. The ID can be used for effects and animations")
        public String arc(
                        @P("Defines the X coordinate of the center point of the arc.") double centerX,
                        @P("Defines the Y coordinate of the center point of the arc.") double centerY,
                        @P("Defines the angular extent of the arc in degrees.") double lengh,
                        @P("Defines the overall width (horizontal radius) of the full ellipse of which this arc is a partial section.") double radiusX,
                        @P("Defines the overall height (vertical radius) of the full ellipse of which this arc is a partial section.") double radiusY,
                        @P("Defines the starting angle of the arc in degrees.") double startAngle,
                        @P("Defines the closure type for the arc: OPEN, CHORD,or ROUND.") ArcType arcType,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {

                var arc = new Arc(centerX, centerY, radiusX, radiusY, startAngle, lengh);
                return setupShape(arc, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a circle and return its ID. The ID can be used for effects and animations")
        public String circle(
                        @P("Defines the horizontal position of the center of the circle in pixels.") double centerX,
                        @P("Defines the vertical position of the center of the circle in pixels.") double centerY,
                        @P("Defines the radius of the circle in pixels.") double radius,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var circle = new Circle(centerX, centerY, radius);
                return setupShape(circle, color, smooth, strokeColor, strokeDashOffset, strokeWidth);

        }

        @Tool("Create an ellipse and return its ID. The ID can be used for effects and animations")
        public String ellipse(
                        @P("Defines the horizontal position of the center of the ellipse in pixels.") double centerX,
                        @P("Defines the vertical position of the center of the ellipse in pixels.") double centerY,
                        @P("Defines the width of the ellipse in pixels.") double radiusX,
                        @P("Defines the height of the ellipse in pixels.") double radiusY,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var ellipse = new Ellipse(centerX, centerY, radiusX, radiusY);
                return setupShape(ellipse, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a line and returns its ID. The ID can be used for effects and animations")
        public String line(
                        @P("The X coordinate of the start point of the line segment.") double startX,
                        @P("The Y coordinate of the start point of the line segment.") double startY,
                        @P("The X coordinate of the end point of the line segment.") double endX,
                        @P("The Y coordinate of the end point of the line segment.") double endY,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var line = new Line(startX, startY, endX, endY);
                return setupShape(line, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a polygon and return its ID. The ID can be used for effects and animations")
        public String polygon(
                        @P("the coordinates of the polygon vertices") double[] points,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var polygon = new Polygon(points);
                return setupShape(polygon, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a polyline and return its ID. The ID can be used for effects and animations")
        public String polyline(
                        @P("the coordinates of the polyline vertices") double[] points,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var polyline = new Polyline(points);
                return setupShape(polyline, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a rectangle shape and return its ID. The ID can be used for effects and animations")
        public String rectangle(
                        @P("Defines the X coordinate of the upper-left corner of the rectangle.") double x,
                        @P("Defines the Y coordinate of the upper-left corner of the rectangle.") double y,
                        @P("Defines the width of the rectangle") double width,
                        @P("Defines the height of the rectangle") double height,
                        @P("Defines the horizontal diameter of the arc at the four corners of the rectangle.") double arcWidth,
                        @P("Defines the vertical diameter of the arc at the four corners of the rectangle.") double arcHeight,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var rectangle = new Rectangle(x, y, width, height);
                rectangle.setArcWidth(arcWidth);
                rectangle.setArcHeight(arcHeight);
                return setupShape(rectangle, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create an arc shape and return its ID. The ID can be used for effects and animations")
        public String svgPath(
                        @P("Defines the SVG Path encoded string as specified at: http://www.w3.org/TR/SVG/paths.html") String content,
                        @P("Defines the filling rule constant for determining the interior of the path.") FillRule fillRule,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var svgPath = new SVGPath();
                svgPath.setContent(content);
                svgPath.setFillRule(fillRule);
                return setupShape(svgPath, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Create a text and return its ID. The ID can be used for effects and animations")
        public String text(
                        String text,
                        @P("Defines the X coordinate of text origin.") double x,
                        @P("Defines the Y coordinate of text origin.") double y,
                        String fontFamily,
                        FontWeight fontWeight,
                        FontPosture fontPosture,
                        int fontSize,
                        TextAlignment alignment,
                        double wrappingWidth,
                        VPos textOrigin,
                        @P("The text fill color") String color,
                        @P("The text stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var font = Font.font(fontFamily, fontWeight, fontPosture, fontSize);
                var txt = new Text(text);
                txt.setFont(font);
                txt.setTextAlignment(alignment);
                txt.setWrappingWidth(wrappingWidth);
                txt.setTextOrigin(textOrigin);
                return setupShape(txt, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Defines a quadratic Bézier parametric curve segment in (x,y) coordinate space  and returns its ID. The ID can be used for effects and animations")
        public String quadCurve(
                        @P("Defines the X coordinate of the start point of the quadratic curve segment.") double startX,
                        @P("Defines the Y coordinate of the start point of the quadratic curve segment.") double startY,
                        @P("Defines the X coordinate of the control point of the quadratic curve segment") double controlX,
                        @P("Defines the Y coordinate of the control point of the quadratic curve segment") double controlY,
                        @P("Defines the X coordinate of the end point of the quadratic curve segment.") double endX,
                        @P("Defines the Y coordinate of the end point of the quadratic curve segment.") double endY,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var quadCurve = new QuadCurve(startX, startY, controlX, controlY, endX, endY);
                return setupShape(quadCurve, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Defines a cubic Bézier parametric curve segment in (x,y) coordinate space and returns its ID. The ID can be used for effects and animations")
        public String cubicCurve(
                        @P("Defines the X coordinate of the start point of the quadratic curve segment.") double startX,
                        @P("Defines the Y coordinate of the start point of the quadratic curve segment.") double startY,
                        @P("Defines the X coordinate of the first control point of the quadratic curve segment") double controlX1,
                        @P("Defines the Y coordinate of the first control point of the quadratic curve segment") double controlY1,
                        @P("Defines the X coordinate of the second control point of the quadratic curve segment") double controlX2,
                        @P("Defines the Y coordinate of the second control point of the quadratic curve segment") double controlY2,
                        @P("Defines the X coordinate of the end point of the quadratic curve segment.") double endX,
                        @P("Defines the Y coordinate of the end point of the quadratic curve segment.") double endY,
                        @P("The shape fill color") String color,
                        @P("The shape stroke color") String strokeColor,
                        @P("If true then antialiasing hints are used") boolean smooth,
                        @P("Defines a distance specified in user coordinates that represents an offset into the dashing pattern.") double strokeDashOffset,
                        @P("The shape stroke width") double strokeWidth) {
                var cubicCurve = new CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
                return setupShape(cubicCurve, color, smooth, strokeColor, strokeDashOffset, strokeWidth);
        }

        @Tool("Group the shapes and return the group ID. The group ID can be used in animations and effects")
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

        @Tool("Return all shapes and groups nodes ID")
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

        @Tool("Animation of the X and Y position of a given shape or group")
        public void animateShapePosition(
                        @P("The node to be animated") String nodeId,
                        @P("The target X position") double byX,
                        @P("The target Y position") double byY,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var translateTransition = new TranslateTransition(Duration.millis(duration), node);
                translateTransition.setByX(byX);
                translateTransition.setByY(byY);
                setupTransition(translateTransition, autoReverse, cycles);
        }

        @Tool("Animation of the X position of a given shape or group")
        public void animateShapeXPosition(
                        @P("The node to be animated") String nodeId,
                        @P("The target X position") double byX,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var translateTransition = new TranslateTransition(Duration.millis(duration), node);
                translateTransition.setByX(byX);
                setupTransition(translateTransition, autoReverse, cycles);
        }

        @Tool("Animation of the X and Y position of a given shape or group")
        public void animateShapeYPosition(
                        @P("The node to be animated") String nodeId,
                        @P("The target Y position") double byY,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var translateTransition = new TranslateTransition(Duration.millis(duration), node);
                translateTransition.setByY(byY);
                setupTransition(translateTransition, autoReverse, cycles);
        }

        @Tool("Animation of the X and Y scale of a given shape or group")
        public void animateShapeSize(
                        @P("The node to be animated") String nodeId,
                        @P("The target X scale. For example, if the value is 1.1, then the X scale will increase by 10%.") double byX,
                        @P("The target Y scape. For example, if the value is 1.1, then the Y scale will increase by 10%.") double byY,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var scale = new ScaleTransition(Duration.millis(duration), node);
                scale.setByX(byX);
                scale.setByY(byY);
                setupTransition(scale, autoReverse, cycles);
        }

        @Tool("Animation of the X size of a given shape or group")
        public void animateShapeXSize(
                        @P("The node to be animated") String nodeId,
                        @P("The target X scale. For example, if the value is 1.1, then the X scale will increase by 10%.") double byX,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var scale = new ScaleTransition(Duration.millis(duration), node);
                scale.setByX(byX);
                setupTransition(scale, autoReverse, cycles);
        }

        @Tool("Animation of the Y size of a given shape or group")
        public void animateShapeYSize(
                        @P("The node to be animated") String nodeId,
                        @P("The target Y scale. For example, if the value is 1.1, then the X scale will increase by 10%.") double byY,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var scale = new ScaleTransition(Duration.millis(duration), node);
                scale.setByY(byY);
                setupTransition(scale, autoReverse, cycles);
        }

        @Tool("Animate the opacity of a given shape or group")
        public void animateShapeOpacity(
                        @P("The node to be animated") String nodeId,
                        @P("The target opacity") double opacity,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var fade = new FadeTransition(Duration.millis(duration), node);
                fade.setByValue(opacity);
                setupTransition(fade, autoReverse, cycles);
        }

        @Tool("Rotate a given shape or group")
        public void animateShapeRotate(
                        @P("The node to be animated") String nodeId,
                        @P("The target angle") double angle,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
                var rotate = new RotateTransition(Duration.millis(duration), node);
                rotate.setByAngle(angle);
                setupTransition(rotate, autoReverse, cycles);
        }

        @Tool("Changes the color of a Shape in a given interval. This only works with Shapes, not groups")
        public void animateShapeColor(
                        @P("The node to be animated") String nodeId,
                        @P("The color to start the transtion. Use null if you want to transition from the shape current color") String fromColor,
                        @P("The target color") String toColor,
                        @P("The animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("The number of cycles. Use -1 for infinite cycles. If the user do not provide the number of cycles, then consider it to be infinite. If the user do not provide the number of cycles, then consider it to be infinite") int cycles) {
                var node = findNodeById(nodeId);
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

        private String setupShape(Shape shape, String color,
                        boolean smooth,
                        String strokeColor,
                        double strokeDashOffset,
                        double strokeWidth) {
                shape.setFill(fixColor(color));
                shape.setStroke(fixColor(color));
                shape.setSmooth(smooth);
                shape.setStrokeDashOffset(strokeDashOffset);
                shape.setStrokeWidth(strokeWidth);
                shape.setId(shape.getClass().getSimpleName().toLowerCase() + SHAPE_ID_CONTROL.incrementAndGet());
                Platform.runLater(() -> container.getChildren().add(shape));
                return shape.getId();
        }

}
