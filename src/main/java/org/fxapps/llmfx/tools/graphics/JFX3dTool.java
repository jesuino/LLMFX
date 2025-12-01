package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.ArrayList;

import org.fxyz3d.geometry.Point3D;

import org.fxyz3d.shapes.Capsule;
import org.fxyz3d.shapes.Cone;
import org.fxyz3d.shapes.Spheroid;

import org.fxyz3d.shapes.primitives.FrustumMesh;
import org.fxyz3d.shapes.primitives.IcosahedronMesh;
import org.fxyz3d.shapes.primitives.OctahedronMesh;
import org.fxyz3d.shapes.primitives.PrismMesh;
import org.fxyz3d.shapes.primitives.PyramidMesh;
import org.fxyz3d.shapes.primitives.TetrahedraMesh;
import org.fxyz3d.shapes.primitives.Text3DMesh;
import org.fxyz3d.shapes.primitives.TrapezoidMesh;

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
import javafx.scene.text.Font;
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
    private SubScene subScene;
    private Group container;
    private Camera camera;

    @PostConstruct
    public void init() {
        this.container = new Group();
        var actualContainer = new Group();
        this.subScene = new SubScene(actualContainer, WIDTH, HEIGHT);
        super.init();

        actualContainer.getChildren().add(container);

        subScene.setPickOnBounds(true);

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
        this.container.getChildren().clear();
        try {
            this.add3dObjects(newContent, true);
        } catch (Exception e) {
            setMessage(e.getMessage());
        }
    }

    @Tool("""
            You can add 3D objects to the scene using the following DSL commands (please use one command per line):
            clear
            color c
            box x y z width height depth
            sphere x y z radius
            cylinder x y z radius height
            cone x y z divs r h
            spheroid x y z divs minorRadius majorRadius
            capsule x y z r h
            pointLight x y z
            tetrahedra x y z h
            octahedron x y z h hypotenuse
            pyramid x y z h hypotenuse
            icosahedron x y z diameter
            trapezoid x y z smallSize bigSize h depth
            frustum x y z majorRadius minorRadius height
            ambientLight c
            """)
    public void add3dObjects(@P("The dsl to add 3d objects") String dsl) {
        try {
            setMessage("");
            add3dObjects(dsl, false);
        } catch (Exception e) {
            setMessage(e.getMessage());
            setEditorContent(dsl);
            e.printStackTrace();
        }
    }

    public void add3dObjects(String dsl, boolean userInput) {
        var material = new PhongMaterial(Color.WHITE);
        var clear = false;
        String[] lines = dsl.split("\\R");
        for (String line : lines) {
            var tokens = line.trim().split("\\s+");
            if (tokens.length == 0 || line.startsWith("#"))
                continue;
            var command = tokens[0];

            var paramsList = new ArrayList<Double>();
            var paramsIdx = "text".equals(command) ? 2 : 1;
            for (int j = paramsIdx; j < tokens.length; j++) {
                var v = tokens[j];

                if ("#".equals(v)) {
                    break;
                }
                if (v == null || v.isBlank() || !canParseToDouble(v)) {
                    continue;
                }
                paramsList.add(Double.parseDouble(v));
            }
            Double[] params = paramsList.toArray(Double[]::new);

            Node element = switch (command) {
                case "color" -> {
                    material = new PhongMaterial(fixColor(tokens[1]));
                    yield null;
                }
                case "box" -> {
                    checkParams(command, params, 6);
                    Box box = new Box(params[3], params[4], params[5]);
                    box.setMaterial(material);
                    box.setTranslateX(params[0]);
                    box.setTranslateY(params[1]);
                    box.setTranslateZ(params[2]);
                    yield box;
                }
                case "sphere" -> {
                    checkParams(command, params, 4);
                    Sphere sphere = new Sphere(params[3]);
                    sphere.setMaterial(material);
                    sphere.setTranslateX(params[0]);
                    sphere.setTranslateY(params[1]);
                    sphere.setTranslateZ(params[2]);
                    yield sphere;
                }
                case "cylinder" -> {
                    checkParams(command, params, 5);
                    Cylinder cylinder = new Cylinder(params[3], params[4]);
                    cylinder.setMaterial(material);
                    cylinder.setTranslateX(params[0]);
                    cylinder.setTranslateY(params[1]);
                    cylinder.setTranslateZ(params[2]);
                    yield cylinder;
                }
                case "cone" -> {
                    checkParams(command, params, 6);
                    var cone = new Cone(params[3].intValue(), params[4], params[5]);
                    cone.setTranslateX(params[0]);
                    cone.setTranslateY(params[1]);
                    cone.setTranslateZ(params[2]);
                    yield cone;
                }
                case "spheroid" -> {
                    checkParams(command, params, 6);
                    var spheroid = new Spheroid(params[3].intValue(), params[4], params[5]);
                    spheroid.setTranslateX(params[0]);
                    spheroid.setTranslateY(params[1]);
                    spheroid.setTranslateZ(params[2]);
                    spheroid.getShape().setMaterial(material);
                    spheroid.setDiffuseColor(material.getDiffuseColor());
                    spheroid.setSpecularColor(material.getSpecularColor());
                    yield spheroid;
                }
                case "tetrahedra" -> {
                    checkParams(command, params, 4);
                    var pos = new Point3D(params[0], params[1], params[2]);
                    var tMesh = new TetrahedraMesh(params[3], 1, pos);
                    tMesh.setMaterial(material);
                    yield tMesh;
                }
                case "capsule" -> {
                    checkParams(command, params, 5);
                    var capsule = new Capsule(params[3], params[4]);
                    capsule.setTranslateX(params[0]);
                    capsule.setTranslateY(params[1]);
                    capsule.setTranslateZ(params[2]);
                    capsule.getShape().setMaterial(material);
                    capsule.setDiffuseColor(material.getDiffuseColor());
                    capsule.setSpecularColor(material.getSpecularColor());
                    yield capsule;
                }
                case "prism" -> {
                    checkParams(command, params, 5);
                    var prism = new PrismMesh(params[3], params[4], 1, null, null);
                    prism.setTranslateX(params[0]);
                    prism.setTranslateY(params[1]);
                    prism.setTranslateZ(params[2]);
                    prism.setMaterial(material);
                    yield prism;
                }
                case "trapezoid" -> {
                    checkParams(command, params, 7);
                    var trapezoid = new TrapezoidMesh(params[3], params[4], params[5], params[6]);
                    trapezoid.setTranslateX(params[0]);
                    trapezoid.setTranslateY(params[1]);
                    trapezoid.setTranslateZ(params[2]);
                    trapezoid.setMaterial(material);
                    yield trapezoid;
                }
                case "octahedron" -> {
                    checkParams(command, params, 5);
                    var octa = new OctahedronMesh(params[3], params[4]);
                    octa.setTranslateX(params[0]);
                    octa.setTranslateY(params[1]);
                    octa.setTranslateZ(params[2]);
                    octa.setMaterial(material);
                    yield octa;
                }
                case "frustum" -> {
                    checkParams(command, params, 6);
                    var frustum = new FrustumMesh(params[3], params[4], params[5]);
                    frustum.setTranslateX(params[0]);
                    frustum.setTranslateY(params[1]);
                    frustum.setTranslateZ(params[2]);
                    frustum.setMaterial(material);
                    yield frustum;
                }
                case "pyramid" -> {
                    checkParams(command, params, 5);
                    var pyramid = new PyramidMesh(params[3], params[4]);
                    pyramid.setTranslateX(params[0]);
                    pyramid.setTranslateY(params[1]);
                    pyramid.setTranslateZ(params[2]);
                    pyramid.setMaterial(material);
                    yield pyramid;
                }
                case "icosahedron" -> {
                    checkParams(command, params, 4);
                    var icosahedron = new IcosahedronMesh(params[3].floatValue());
                    icosahedron.setTranslateX(params[0]);
                    icosahedron.setTranslateY(params[1]);
                    icosahedron.setTranslateZ(params[2]);
                    icosahedron.setMaterial(material);
                    yield icosahedron;
                }
                case "text" -> {
                    // text value x y z fontSize || not working
                    var txt = tokens[1];
                    var text3d = new Text3DMesh(txt, Font.getDefault().getFamily(), params[3].intValue());
                    text3d.setTranslateX(params[0]);
                    text3d.setTranslateY(params[1]);
                    text3d.setTranslateZ(params[2]);
                    text3d.setTextureModeNone(material.getDiffuseColor());
                    yield text3d;

                }
                case "pointLight" -> {
                    checkParams(command, params, 3);
                    var light = new PointLight(Color.WHITE);
                    light.getTransforms().addAll(
                            new Translate(params[0], params[1], params[2]));
                    yield light;
                }
                case "ambientLight" -> {
                    yield new AmbientLight(fixColor(tokens[1]));

                }
                case "clear" -> {
                    Platform.runLater(() -> container.getChildren().clear());
                    clear = true;
                    yield null;
                }
                default -> null;
            };

            if (element != null) {
                Platform.runLater(() -> container.getChildren().add(element));
            }
        }
        if (!userInput) {
            var newContent = clear ? dsl : getEditorContent() + "\n" + dsl;
            setEditorContent(newContent);
        }
    }

    private boolean canParseToDouble(String v) {
        try {
            Double.parseDouble(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

    private void checkParams(String command, Double[] params, int expected) {
        if (params.length < expected) {
            throw new IllegalArgumentException(
                    "Command '" + command + "' expects " + expected + " parameters, but got " + params.length);
        }
    }

}
