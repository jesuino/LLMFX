package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import org.fxapps.llmfx.Events.New3DContentEvent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;


// TODO: improve the manipulation of the camera and light
@Singleton
public class JFX3dTool {

    @Inject
    Event<New3DContentEvent> new3DContentEvent;

    @Tool("""
            Configures the camera for the 3D scene. You can set the camera to be a perspective or parallel camera.
            You can also set the camera position and rotation.
            The camera defines the mapping of the scene coordinate space onto the window.
            """)
    public void configureCamera(
            @P("true if it is a perspective camera") boolean isPerspective,
            @P("true if the the eye position is fixed at (0, 0, 0) in the local coordinates of the camera") boolean fixedEyeAtCameraZero,
            @P("add x rotation transform") Double xRotate,
            @P("add y rotation transform") Double yRotate,
            @P("add z rotation transform") Double zRotate,
            @P("add x translate transform") Double x,
            @P("add y rotation transform") Double y,
            @P("add z rotation transform") Double z) {
        Camera camera = isPerspective ? new PerspectiveCamera(true) : new ParallelCamera();

        camera.getTransforms().addAll(
                new Rotate(yRotate, Rotate.Y_AXIS),
                new Rotate(xRotate, Rotate.X_AXIS),
                new Translate(x, y, z));

        new3DContentEvent.fire(new New3DContentEvent(camera));

    }

    @Tool("""
            Defines a point light source object. A light source that has a fixed point in space and radiates light equally in all directions away from itself.

            """)
    public void addPointLight(@P("The light color in web format") String color,
            @P("The light x position") double x,
            @P("The light y position") double y,
            @P("The light z position") double z) {
        var pointLight = new PointLight(fixColor(color));
        pointLight.setTranslateX(x);
        pointLight.setTranslateY(y);
        pointLight.setTranslateZ(z);
        new3DContentEvent.fire(new New3DContentEvent(pointLight));
    }

    @Tool("""
            Defines an ambient light source object. A light source that has a fixed point in space and radiates light equally in all directions away from itself.

            """)
    public void addAmbientLight(@P("The light color in web format") String color) {
        var pointLight = new AmbientLight(fixColor(color));
        new3DContentEvent.fire(new New3DContentEvent(pointLight));
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

        new3DContentEvent.fire(new New3DContentEvent(box));

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
        new3DContentEvent.fire(new New3DContentEvent(sphere));

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
        new3DContentEvent.fire(new New3DContentEvent(cylinder));

    }

}
