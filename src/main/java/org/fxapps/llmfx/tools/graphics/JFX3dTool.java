package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.fxapps.llmfx.tools.dsl.CommandFunctionRegistry;
import org.fxapps.llmfx.tools.dsl.DSLParser;
import org.fxyz3d.geometry.Point3D;

import org.fxyz3d.shapes.primitives.CapsuleMesh;
import org.fxyz3d.shapes.primitives.ConeMesh;
import org.fxyz3d.shapes.primitives.FrustumMesh;
import org.fxyz3d.shapes.primitives.IcosahedronMesh;
import org.fxyz3d.shapes.primitives.OctahedronMesh;
import org.fxyz3d.shapes.primitives.PrismMesh;
import org.fxyz3d.shapes.primitives.PyramidMesh;
import org.fxyz3d.shapes.primitives.SpheroidMesh;
import org.fxyz3d.shapes.primitives.TetrahedraMesh;
import org.fxyz3d.shapes.primitives.TrapezoidMesh;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SpotLight;
import javafx.scene.SubScene;
import javafx.scene.paint.Material;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

// TODO: improve the manipulation of the camera and light

// perhaps improve this with FXyz
// perhaps add controls to manipulate the 3d Scene?
@Singleton
public class JFX3dTool extends EditorJFXTool {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int ANGLE_STEP = 1;

    private static final Material DEFAULT_MATERIAL = new PhongMaterial(Color.WHITE);
    private SubScene subScene;
    private Group container;
    private Camera camera;

    CommandFunctionRegistry<Shape3D> cmdFnRegistry;

    AtomicReference<Material> materialRef;

    @PostConstruct
    public void init() {
        this.materialRef = new AtomicReference<>(DEFAULT_MATERIAL);
        this.container = new Group();
        var actualContainer = new Group();
        this.subScene = new SubScene(actualContainer, WIDTH, HEIGHT);
        super.init();

        actualContainer.getChildren().add(container);

        subScene.setPickOnBounds(true);

        // TODO: Extract this to a different control object

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-50);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        this.subScene.setCamera(camera);

        final var rotateX = new Rotate(0, Rotate.X_AXIS);
        final var rotateY = new Rotate(0, Rotate.Y_AXIS);
        final var rotateZ = new Rotate(0, Rotate.Z_AXIS);
        final var translate = new Translate(0, 0, 0);
        container.getTransforms().addAll(rotateX, rotateY, rotateZ);
        camera.getTransforms().add(translate);

        subScene.setFill(Color.LIGHTGRAY);

        addStuff();

        initCommandsRegistry();
        var previousPos = new double[2];

        this.container.setOnMouseDragEntered(e -> {
            previousPos[0] = e.getSceneX();
            previousPos[1] = e.getSceneY();
        });

        this.subScene.setOnMouseDragged(e -> {
            final var x = e.getSceneX();
            final var y = e.getSceneY();

            if (e.isPrimaryButtonDown()) {
                var xChange = 0d;
                var yChange = 0d;
                if (x > previousPos[0]) {
                    xChange = -0.2;
                } else if (x < previousPos[0]) {
                    xChange = 0.2;
                }
                if (y > previousPos[1]) {
                    yChange = -0.2;
                } else if (y < previousPos[1]) {
                    yChange = 0.2;
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
                rotateX.setAngle(0);
                rotateY.setAngle(0);
                rotateZ.setAngle(0);
                translate.setX(0);
                translate.setY(0);
                translate.setZ(0);
            }
        });
        this.subScene.setOnScroll(e -> {
            subScene.requestFocus();
            if (e.getDeltaY() > 0d) {
                translate.setZ(translate.getZ() + 1);
            } else if (e.getDeltaY() < 0d) {
                translate.setZ(translate.getZ() - 1);
            }
        });

    }

    private void initCommandsRegistry() {
        cmdFnRegistry = new CommandFunctionRegistry<>();
        cmdFnRegistry.register("box",
                params -> {
                    var x = params.getDouble(3);
                    var y = params.size() > 4 ? params.getDouble(4) : x;
                    var z = params.size() > 5 ? params.getDouble(5) : x;
                    return new Box(x, y, z);
                });
        cmdFnRegistry.register("sphere",
                params -> new Sphere(params.getDouble(3)));
        cmdFnRegistry.register("cylinder",
                params -> new Cylinder(params.getDouble(3),
                        params.getDouble(4)));
        cmdFnRegistry.register("cone",
                params -> {
                    var sides = params.getInt(3);
                    if (sides < 2) {
                        throw new IllegalArgumentException("A cone must have at least 2 sides");
                    }
                    return new ConeMesh(sides,
                            params.getDouble(4),
                            params.getDouble(5));
                });
        cmdFnRegistry.register("spheroid",
                params -> new SpheroidMesh(params.getInt(3),
                        params.getDouble(4),
                        params.getDouble(5)));
        cmdFnRegistry.register("tetrahedra",
                params -> {
                    var pos = new Point3D(params.getDouble(0),
                            params.getDouble(1),
                            params.getDouble(2));
                    return new TetrahedraMesh(params.getDouble(3), 1, pos);
                });
        cmdFnRegistry.register("capsule",
                params -> new CapsuleMesh(params.getDouble(3),
                        params.getDouble(4)));
        cmdFnRegistry.register("prism",
                params -> new PrismMesh(params.getDouble(3),
                        params.getDouble(4),
                        1,
                        null,
                        null));
        cmdFnRegistry.register("trapezoid",
                params -> new TrapezoidMesh(params.getDouble(3),
                        params.getDouble(4),
                        params.getDouble(5),
                        params.getDouble(6)));
        cmdFnRegistry.register("octahedron",
                params -> new OctahedronMesh(params.getDouble(3),
                        params.getDouble(4)));
        cmdFnRegistry.register("frustum",
                params -> new FrustumMesh(params.getDouble(3),
                        params.getDouble(4),
                        params.getDouble(5)));
        cmdFnRegistry.register("pyramid",
                params -> new PyramidMesh(params.getDouble(3),
                        params.getDouble(4)));
        cmdFnRegistry.register("icosahedron",
                params -> new IcosahedronMesh(params.getDouble(3).floatValue()));
        cmdFnRegistry.register("pointLight",
                params -> {
                    var light = new PointLight(fixColor(params.get(0)));
                    light.getTransforms().addAll(
                            new Translate(params.getDouble(1),
                                    params.getDouble(2),
                                    params.getDouble(3)));
                    this.container.getChildren().add(light);
                    return null;
                });
        cmdFnRegistry.register("directionalLight",
                params -> {
                    var light = new DirectionalLight(fixColor(params.get(0)));
                    light.setDirection(new javafx.geometry.Point3D(params.getDouble(1),
                            params.getDouble(2),
                            params.getDouble(3)));
                    this.container.getChildren().add(light);
                    return null;
                });
        cmdFnRegistry.register("spotLight",
                params -> {
                    var light = new SpotLight(fixColor(params.get(0)));
                    light.setTranslateX(params.getDouble(1));
                    light.setTranslateY(params.getDouble(2));
                    light.setTranslateZ(params.getDouble(3));
                    light.setDirection(new javafx.geometry.Point3D(params.getDouble(4),
                            params.getDouble(5),
                            params.getDouble(6)));
                    light.setInnerAngle(params.getDouble(7));
                    light.setOuterAngle(params.getDouble(8));
                    light.setFalloff(2);
                    this.container.getChildren().add(light);
                    return null;
                });
        cmdFnRegistry.register("ambientLight",
                params -> {
                    this.container.getChildren()
                            .add(new AmbientLight(fixColor(params.getStr(0))));
                    return null;
                });

        cmdFnRegistry.register("color",
                params -> {
                    materialRef.set(new PhongMaterial(fixColor(params.get(0))));
                    return null;
                });
    }

    @Override
    Node getRenderNode() {
        return subScene;
    }

    @Override
    void clearRenderNode() {
        materialRef.set(DEFAULT_MATERIAL);
        container.getChildren().clear();
    }

    @Override
    void onEditorChange(String newContent) {
        clearRenderNode();
        try {
            this.add3dObjects(newContent, true);
        } catch (Exception e) {
            setMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    @Tool("""
            You can add 3D objects to the scene using the following DSL commands.
            You don't need to specify the parameter name you should use one command per line.
            You can add comments using #

            DSL commands:

            clear
            color c # web color
            box x y z width height depth
            sphere x y z radius
            cylinder x y z radius height
            cone x y z divs r h
            spheroid x y z divs minorRadius majorRadius
            capsule x y z r h
            tetrahedra x y z h
            octahedron x y z h hypotenuse
            pyramid x y z h hypotenuse
            icosahedron x y z diameter
            trapezoid x y z smallSize bigSize h depth
            frustum x y z majorRadius minorRadius height
            pointLight color x y z # web color
            directionalLight color dx dy dz # web color
            spotLight color x y z dx dy dz innerAngle outerAngle # web color
            ambientLight color # web color
            """)
    public void add3dObjects(@P("The dsl to add 3d objects") String dsl) {
        try {
            setMessage("");
            add3dObjects(dsl, false);
        } catch (Exception e) {
            setMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    public void add3dObjects(String dsl, boolean userInput) {
        var clear = false;
        var commands = DSLParser.parse(dsl);
        for (var command : commands) {
            var params = command.params();
            Optional<Shape3D> shape3dOp = switch (command.name()) {
                case "clear" -> {
                    Platform.runLater(() -> clearRenderNode());
                    clear = true;
                    yield Optional.empty();
                }
                default -> cmdFnRegistry.run(command);
            };

            shape3dOp.ifPresent(shape3d -> {
                shape3d.setTranslateX(params.getDouble(0));
                shape3d.setTranslateY(params.getDouble(1));
                shape3d.setTranslateZ(params.getDouble(2));
                shape3d.setMaterial(materialRef.get());
                Platform.runLater(() -> container.getChildren().add(shape3d));
            });

        }
        if (!userInput) {
            var newContent = clear ? dsl : getEditorContent() + "\n" + dsl;
            setEditorContent(newContent);
        }
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
