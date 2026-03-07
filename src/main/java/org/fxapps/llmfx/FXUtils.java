package org.fxapps.llmfx;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import org.fxapps.llmfx.tools.dsl.Param;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class FXUtils {

    

    public static Color fixColor(Param param) {
        return fixColor(param.value());
    }

    // this is a workaround to convert the color provided by the LLM to a valid
    // color
    // I see that sometimes it halucinates or provides invalid colors and JavaFX is
    // strict
    // about the color format. So we need to fix it.
    public static Color fixColor(String color) {
        if (color == null || "none".equalsIgnoreCase(color) || "null".equalsIgnoreCase(color)) {
            return Color.TRANSPARENT;
        }
        try {
            return Color.valueOf(color);
        } catch (IllegalArgumentException e) {
            // if the color is not valid, return a default color
            return Color.TRANSPARENT;
        }
    }

    // had to create this because SwingFXUtils is bringing me classloading
    // conflicts:
    // java.lang.NoClassDefFoundError: com/sun/javafx/collections/MappingChange$Map
    // Code got from:
    // https://github.com/openjdk/jfx/blob/master/modules/javafx.swing/src/main/java/javafx/embed/swing/SwingFXUtils.java

    public static BufferedImage fromFXImage(Image img) {
        PixelReader pr = img.getPixelReader();
        if (pr == null) {
            return null;
        }
        int iw = (int) img.getWidth();
        int ih = (int) img.getHeight();
        PixelFormat<?> fxFormat = pr.getPixelFormat();
        int prefBimgType = switch (fxFormat.getType()) {
            case BYTE_BGRA_PRE, INT_ARGB_PRE -> BufferedImage.TYPE_INT_ARGB_PRE;
            case BYTE_BGRA, INT_ARGB -> BufferedImage.TYPE_INT_ARGB;
            case BYTE_RGB -> BufferedImage.TYPE_INT_RGB;
            case BYTE_INDEXED ->
                (fxFormat.isPremultiplied()
                        ? BufferedImage.TYPE_INT_ARGB_PRE
                        : BufferedImage.TYPE_INT_ARGB);
        };

        var bimg = new BufferedImage(iw, ih, prefBimgType);

        DataBufferInt db = (DataBufferInt) bimg.getRaster().getDataBuffer();
        int data[] = db.getData();
        int offset = bimg.getRaster().getDataBuffer().getOffset();
        int scan = 0;
        SampleModel sm = bimg.getRaster().getSampleModel();
        if (sm instanceof SinglePixelPackedSampleModel sppsm) {
            scan = sppsm.getScanlineStride();
        }

        var pf = switch (bimg.getType()) {
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB_PRE -> PixelFormat.getIntArgbPreInstance();
            case BufferedImage.TYPE_INT_ARGB -> PixelFormat.getIntArgbInstance();
            default -> throw new InternalError("Failed to validate BufferedImage type");
        };
        pr.getPixels(0, 0, iw, ih, pf, data, offset, scan);
        return bimg;
    }

}
