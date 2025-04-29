package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.geometry.Point3D;
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
public class JFX3dTool {

        private SubScene subScene;
        private Group container;

        public void setSubScene(SubScene subScene, Group container) {
                this.subScene = subScene;
                this.container = container;

                Camera camera = new PerspectiveCamera(true);
                camera.setFarClip(6000);
                camera.setNearClip(0.01);
                camera.setTranslateY(-2000);
                camera.getTransforms().addAll(
                                new Rotate(-90, Rotate.X_AXIS),
                                new Rotate(0, Rotate.Y_AXIS),
                                new Rotate(-90, Rotate.Z_AXIS));

                this.container.getChildren().add(camera);
                this.subScene.setCamera(camera);
                subScene.setFill(Color.SILVER);
        }

        @Tool("rotate the view")
        public void rotateCamera(
                        @P("add x rotation transform") Double x,
                        @P("add y rotation transform") Double y,
                        @P("add z rotation transform") Double z) {

                Platform.runLater(() -> {
                        subScene.getCamera()
                                        .getTransforms()
                                        .removeIf(t -> t instanceof Rotate);
                        subScene.getCamera()
                                        .getTransforms()
                                        .addAll(new Rotate(x, Rotate.X_AXIS),
                                                        new Rotate(y, Rotate.Y_AXIS),
                                                        new Rotate(z, Rotate.Z_AXIS));

                });
        }

        @Tool("move the camera")
        public void moveCamera(
                        @P("add x translate transform") Double x,
                        @P("add y rotation transform") Double y,
                        @P("add z rotation transform") Double z) {
                Platform.runLater(() -> {
                        subScene.getCamera()
                                        .getTransforms()
                                        .removeIf(t -> t instanceof Translate);

                        subScene.getCamera()
                                        .getTransforms()
                                        .add(new Translate(x, y, z));
                });
        }

        @Tool("""
                        Defines a point light source object.
                        A light source that has a fixed point in space and radiates light equally in all directions away from itself.

                        """)
        public void addPointLight(@P("The light color in web format") String color,
                        @P("The light x position") double x,
                        @P("The light y position") double y,
                        @P("The light z position") double z) {
                var pointLight = new PointLight(fixColor(color));
                pointLight.setTranslateX(x);
                pointLight.setTranslateY(y);
                pointLight.setTranslateZ(z);
                add(pointLight);
        }

        @Tool("""
                        Adds a box to the 3d scene
                        """)
        public void addBox(@P("the width or the X dimension of the Box") double width,
                        @P("the height or the Y dimension of the Box.") double height,
                        @P("the depth or the Z dimension of the Box.") double depth,
                        @P("Box x position") double x,
                        @P("Box y position") double y,
                        @P("Box z position") double z,
                        @P("Box diffuse color in web format") String diffuseColor,
                        @P("Box specular color in web format") String specularColor) {
                var box = new Box(width, height, depth);
                box.getTransforms().add(new Translate(x, y, z));
                var material = new PhongMaterial();
                material.setDiffuseColor(fixColor(diffuseColor));
                material.setSpecularColor(fixColor(specularColor));
                box.setMaterial(material);

                add(box);

        }

        @Tool("""
                        Adds a Sphere to the 3d scene
                        """)
        public void addSphere(
                        @P("Sphere radius") double radius,
                        @P("Sphere x position") double x,
                        @P("Sphere y position") double y,
                        @P("Sphere z position") double z,
                        @P("Sphere diffuse color in web format") String diffuseColor,
                        @P("Sphere specular color in web format") String specularColor) {
                var sphere = new Sphere(radius);
                sphere.getTransforms().add(new Translate(x, y, z));

                final var material = new PhongMaterial();
                material.setSpecularColor(fixColor(specularColor));
                material.setDiffuseColor(fixColor(diffuseColor));
                sphere.setMaterial(material);
                add(sphere);

        }

        @Tool("""
                        Adds a cylinder to the 3d scene
                        """)
        public void addCilinder(
                        @P("Cylinder radius") double radius,
                        @P("Cylinder height") double height,
                        @P("Cylinder x position") double x,
                        @P("Cylinder y position") double y,
                        @P("Cylinder z position") double z,
                        @P("Cylinder diffuse color in web format") String diffuseColor,
                        @P("Cylinder specular color in web format") String specularColor) {
                var cylinder = new Cylinder(radius, height);
                cylinder.getTransforms().add(new Translate(x, y, z));

                final PhongMaterial material = new PhongMaterial();
                material.setSpecularColor(fixColor(specularColor));
                material.setDiffuseColor(fixColor(diffuseColor));
                cylinder.setMaterial(material);
                add(cylinder);
        }

        private void add(Node node) {
                Platform.runLater(() -> container.getChildren().add(node));
        }

}
