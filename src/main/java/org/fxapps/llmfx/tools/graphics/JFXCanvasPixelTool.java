package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

@Singleton
public class JFXCanvasPixelTool {

    private Canvas canvas;

    private GraphicsContext ctx;

    // later we can let the LLM set a resolution
    private static final int RESOLUTION = 15;

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getGraphicsContext2D();
    }

    @Tool("""
            Returns the canvas Max Size (width vs height). use it as the boundaries when
            writing pixels.
            You are not required to fill all pixels, just make sure that this is the
            boundary
            """)
    public double[] getMaxSize() {
        return new double[] { canvas.getWidth() / RESOLUTION, canvas.getHeight() / RESOLUTION };
    }

   // @Tool("Writes pixels at he canvas. It receives all pixels values at once")
    public void writePixelsColors(
            @P("""
                    The matrix of colors to be written. Each matrix position matches a pixel x,y position.
                    The color is in web format
                        """) String[][] colors) {

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                ctx.setFill(fixColor(colors[i][j]));
                ctx.fillRect(i * RESOLUTION, j * RESOLUTION, RESOLUTION, RESOLUTION);
            }
        }
    }

    @Tool("Writes pixels at specific position and web color")
    public void writePixelColor(double x, double y, String color) {
        ctx.setFill(fixColor(color));
        ctx.fillRect(x * RESOLUTION, y * RESOLUTION, RESOLUTION, RESOLUTION);
    }

}
