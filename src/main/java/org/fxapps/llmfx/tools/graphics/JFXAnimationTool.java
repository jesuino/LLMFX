package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

@Singleton
public class JFXAnimationTool {

        private Group container;

        public void setContainer(Group container) {
                this.container = container;
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
                        @P("Number of cycles or -1 for infinite cycles") int cycles) {
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

        @Tool("Animation of the X and Y scale of a given item")
        public void animateShapeSize(
                        @P("The item to be animated") String itemId,
                        @P("target X scale. If the value is 1.1, then the X scale will increase by 10%. Use null to animate only Y") Double x,
                        @P("target Y scale. If the value is 1.1, then the Y scale will increase by 10%. Use null to animate only X") Double y,
                        @P("Duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("Number of cycles or -1 for infinite cycles") int cycles) {

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
                        @P("Number of cycles or -1 for infinite cycles") int cycles) {
                var node = findNodeById(itemId);
                var fade = new FadeTransition(Duration.millis(duration), node);
                fade.setFromValue(startOpacity);
                fade.setToValue(endOpacity);
                setupTransition(fade, autoReverse, cycles);
        }

        @Tool("Rotate an item")
        public void animateShapeRotate(
                        @P("Item to be animated") String nodeId,
                        @P("Target angle") double angle,
                        @P("Duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("Number of cycles or -1 for infinite cycles") int cycles) {
                var node = findNodeById(nodeId);
                var rotate = new RotateTransition(Duration.millis(duration), node);
                rotate.setByAngle(angle);
                setupTransition(rotate, autoReverse, cycles);
        }

        @Tool("Changes the color of a Shape in a given interval")
        public void animateShapeColor(
                        @P("Shape to be animated") String shapeId,
                        @P("color to start the transtion. Use null if you want to transition from the shape current color") String fromColor,
                        @P("target color") String toColor,
                        @P("animation duration in milliseconds") double duration,
                        @P("If true, then the animation will auto reverse") boolean autoReverse,
                        @P("Number of cycles or -1 for infinite cycles") int cycles) {
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

        private Node findNodeById(String id) {
                return container.getChildren()
                                .stream()
                                .filter(n -> id.equals(n.getId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + id + " not found"));
        }

}
