package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.stream.IntStream;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

// TODO: improve the manipulation of the camera and light

// perhaps improve this with FXyz
// perhaps add controls to manipulate the 3d Scene?
@Singleton
public class JFX3dTool extends EditorJFXTool {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 900;
    private static final int ANGLE_STEP = 1;
    private SubScene subScene;
    private Group container;
    private Camera camera;

    @PostConstruct
    public void init() {
        this.container = new Group();
        this.subScene = new SubScene(container, WIDTH, HEIGHT);
        super.init();

        subScene.setPickOnBounds(true);

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-50);
        camera.setNearClip(0.1);
        camera.setFarClip(100);

        this.subScene.setCamera(camera);

        subScene.setFill(Color.LIGHTGRAY);

        addStuff();

        var previousPos = new double[2];

        this.container.setOnMouseDragEntered(e -> {
            previousPos[0] = e.getSceneX();
            previousPos[1] = e.getSceneY();
        });

        this.subScene.setOnMouseDragged(e -> {
            final var x = e.getSceneX();
            final var y = e.getSceneY();

            var rotateY = container.getTransforms().stream()
                    .filter(t -> t instanceof Rotate r && r.getAxis().equals(Rotate.Y_AXIS))
                    .map(t -> (Rotate) t)
                    .findAny()
                    .orElseGet(() -> {
                        var r = new Rotate(0, Rotate.Y_AXIS);
                        container.getTransforms().add(r);
                        return r;
                    });
            var rotateX = container.getTransforms().stream()
                    .filter(t -> t instanceof Rotate r && r.getAxis().equals(Rotate.X_AXIS))
                    .map(t -> (Rotate) t)
                    .findAny()
                    .orElseGet(() -> {
                        var r = new Rotate(0, Rotate.X_AXIS);
                        container.getTransforms().add(r);
                        return r;
                    });

            var rotateZ = container.getTransforms().stream()
                    .filter(t -> t instanceof Rotate r && r.getAxis().equals(Rotate.Z_AXIS))
                    .map(t -> (Rotate) t)
                    .findAny()
                    .orElseGet(() -> {
                        var r = new Rotate(0, Rotate.Z_AXIS);
                        container.getTransforms().add(r);
                        return r;
                    });

            var translate = camera.getTransforms().stream()
                    .filter(t -> t instanceof Translate)
                    .map(t -> (Translate) t)
                    .findAny()
                    .orElseGet(() -> {
                        var tr = new Translate();
                        camera.getTransforms().add(tr);
                        return tr;
                    });

            if (e.isPrimaryButtonDown()) {
                var xChange = 0d;
                var yChange = 0d;
                if (x > previousPos[0]) {
                    xChange = -0.5;
                } else if (x < previousPos[0]) {
                    xChange = 0.5;
                }
                if (y > previousPos[1]) {
                    yChange = -0.5;
                } else if (y < previousPos[1]) {
                    yChange = 0.5;
                }

                translate.setX(translate.getX() + xChange);
                translate.setY(translate.getY() + yChange);
            }

            if (e.isSecondaryButtonDown()) {
                if (x > previousPos[0]) {
                    rotateY.setAngle(rotateY.getAngle() - ANGLE_STEP);
                } else if (x < previousPos[0]) {
                    rotateY.setAngle(rotateY.getAngle() + ANGLE_STEP);
                }
                if (y > previousPos[1]) {
                    rotateX.setAngle(rotateX.getAngle() + ANGLE_STEP);
                } else if (y < previousPos[1]) {
                    rotateX.setAngle(rotateX.getAngle() - ANGLE_STEP);
                }
            }

            if (e.isSecondaryButtonDown() && e.isControlDown()) {
                if (x > previousPos[0]) {
                    rotateZ.setAngle(rotateY.getAngle() - ANGLE_STEP);
                } else if (x < previousPos[0]) {
                    rotateZ.setAngle(rotateY.getAngle() + ANGLE_STEP);
                }
            }
            previousPos[0] = x;
            previousPos[1] = y;

        });

        this.subScene.setOnMousePressed(e -> {
            subScene.requestFocus();
            if (e.isMiddleButtonDown()) {
                container.getTransforms().clear();
                camera.getTransforms().clear();
            }
        });
        this.subScene.setOnScroll(e -> {
            subScene.requestFocus();
            var translate = camera.getTransforms().stream()
                    .filter(t -> t instanceof Translate)
                    .map(t -> (Translate) t)
                    .findAny()
                    .orElseGet(() -> {
                        var tr = new Translate();
                        camera.getTransforms().add(tr);
                        return tr;
                    });
            if (e.getDeltaY() > 0d) {
                translate.setZ(translate.getZ() + 1);
            } else if (e.getDeltaY() < 0d) {
                translate.setZ(translate.getZ() - 1);
            }
        });

    }

    @Override
    Node getRenderNode() {
        return subScene;
    }

    @Override
    void clearRenderNode() {
        container.getChildren().clear();
    }

    @Override
    void onEditorChange(String newContent) {
        this.add3dObjects(newContent);
    }

    @Tool("""
            You can add 3D objects to the scene using the following DSL
            color r g b   # use this color for subsequent objects. Use RGB values 0-255
            box x y z width height depth
            sphere x y z radius
            cylinder x y z radius height
            pointLight x y z
            ambientLight r g b # Use RGB values 0-255 for the color
            """)
    public void add3dObjects(@P("The dsl to add 3d objects") String dsl) {
        Platform.runLater(() -> container.getChildren().clear());
        var material = new PhongMaterial(Color.WHITE);
        String[] lines = dsl.split("\\R");
        for (String line : lines) {
            var tokens = line.trim().split("\\s+");
            if (tokens.length == 0)
                continue;
            var command = tokens[0];
            double[] params = IntStream.range(1, tokens.length)
                    .mapToObj(i -> tokens[i])
                    .filter(v -> v != null && !v.isBlank())
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            Node element = switch (command) {
                case "color" -> {
                    var r = (int) params[0];
                    var g = (int) params[1];
                    var b = (int) params[2];
                    material = new PhongMaterial(Color.rgb(r, g, b));
                    yield null;
                }
                case "box" -> {
                    Box box = new Box(params[3], params[4], params[5]);
                    box.setMaterial(material);
                    box.setTranslateX(params[0]);
                    box.setTranslateY(params[1]);
                    box.setTranslateZ(params[2]);
                    yield box;
                }
                case "sphere" -> {
                    Sphere sphere = new Sphere(params[3]);
                    sphere.setMaterial(material);
                    sphere.setTranslateX(params[0]);
                    sphere.setTranslateY(params[1]);
                    sphere.setTranslateZ(params[2]);
                    yield sphere;
                }
                case "cylinder" -> {
                    Cylinder cylinder = new Cylinder(params[3], params[4]);
                    cylinder.setMaterial(material);
                    cylinder.setTranslateX(params[0]);
                    cylinder.setTranslateY(params[1]);
                    cylinder.setTranslateZ(params[2]);
                    yield cylinder;
                }
                case "pointLight" -> {
                    var light = new PointLight(Color.WHITE);
                    light.getTransforms().addAll(
                            new Translate(params[0], params[1], params[2]));
                    yield light;
                }
                case "ambientLight" -> {
                    var r = (int) params[0];
                    var g = (int) params[1];
                    var b = (int) params[2];
                    yield new AmbientLight(Color.rgb(r, g, b));

                }
                default -> null;
            };

            if (element != null) {
                Platform.runLater(() -> container.getChildren().add(element));
            }
        }
        setEditorContent(dsl);
    }

    void addStuff() {
        Platform.runLater(() -> {
            Box box = new Box(2, 2, 2);
            box.setMaterial(new PhongMaterial(fixColor("red")));
            box.setTranslateX(-10);

            Sphere sphere = new Sphere(4);
            sphere.setMaterial(new PhongMaterial(fixColor("green")));

            Cylinder cylinder = new Cylinder(4, 2);
            cylinder.setMaterial(new PhongMaterial(fixColor("blue")));
            cylinder.setTranslateX(10);

            PointLight light = new PointLight(Color.WHITE);
            light.getTransforms().addAll(
                    new Translate(0, -10, -10));

            container.getChildren().addAll(box, sphere, cylinder, light);
        });
    }

}
