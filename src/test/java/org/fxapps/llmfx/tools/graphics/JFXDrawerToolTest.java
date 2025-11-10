package org.fxapps.llmfx.tools.graphics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.paint.Color;

class JFXDrawerToolTest {

    private JFXDrawerTool tool;

    @BeforeEach
    public void setup() {
        tool = new JFXDrawerTool();
        tool.gc = mock();
    }

    @Test
    public void testDSL() {
        tool.draw("""
                background 0 10 20
                color 255 200 10
                rect 1 2 3 4
                circle 1 2 3
                fillRect 4 3 2 1
                fillCircle 1 2 3
                width 1
                line 1 2 3 4
                text 1 2 "abc"
                """);
        var color = Color.rgb(255, 200, 10);
        var bg = Color.rgb(0, 10, 20);
        // background 0 10 20
        verify(tool.gc).setFill(eq(bg));
        //verify(tool.gc).fillRect(eq(0), eq(0), eq(JFXDrawerTool.CANVAS_WIDTH), eq(JFXDrawerTool.CANVAS_HEIGHT));
        verify(tool.gc).setFill(color);

        // color 255 200 10
        verify(tool.gc).setFill(eq(color));
        verify(tool.gc).setStroke(eq(color));

        verify(tool.gc).strokeRect(eq(1.0d), eq(2.0d), eq(3.0d), eq(4.0d));
        verify(tool.gc).strokeOval(eq(1.0d), eq(2.0d), eq(3.0d), eq(3.0d));
        verify(tool.gc).fillRect(eq(4.0d), eq(3.0d), eq(2.0d), eq(1.0d));
        verify(tool.gc).fillOval(eq(1.0d), eq(2.0d), eq(3.0d), eq(3.0d));
        verify(tool.gc).setLineWidth(eq(1.0d));
        verify(tool.gc).strokeLine(eq(1.0d), eq(2.0d), eq(3.0d), eq(4.0d));
        verify(tool.gc).fillText(eq("abc"), eq(1.0d), eq(2.0d));
    }

}