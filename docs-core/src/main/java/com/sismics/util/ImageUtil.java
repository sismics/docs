package com.sismics.util;

import com.google.common.hash.Hashing;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Image processing utilities.
 *
 * @author jtremeaux
 */
public class ImageUtil {
    /**
     * Write a high quality JPEG.
     * 
     * @param image Image
     * @param outputStream Output stream
     * @throws IOException e
     */
    public static void writeJpeg(BufferedImage image, OutputStream outputStream) throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = null;
        ImageOutputStream imageOutputStream = null;
        try {
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(1.f);
            imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);

            if (image.getColorModel().hasAlpha()) {
                // Strip alpha channel
                BufferedImage noAlphaImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics graphics = noAlphaImage.getGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                graphics.drawImage(image, 0, 0, null);
                image = noAlphaImage;
            }

            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
        } finally {
            if (imageOutputStream != null) {
                try {
                    imageOutputStream.close();
                } catch (Exception inner) {
                    // NOP
                }
            }
            if (writer != null) {
                writer.dispose();
            }
        }
    }
    
    /**
     * Compute Gravatar hash.
     * See https://en.gravatar.com/site/implement/hash/.
     * 
     * @param email Email
     * @return Gravatar hash
     */
    @SuppressWarnings("deprecation") // Gravatar uses MD5, nothing we can do about it
    public static String computeGravatar(String email) {
        if (email == null) {
            return null;
        }

        return Hashing.md5().hashString(
                email.trim().toLowerCase(), StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Return true if a pixel is black.
     *
     * @param image Image
     * @param x X
     * @param y Y
     * @return True if black
     */
    public static boolean isBlack(BufferedImage image, int x, int y) {
        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            WritableRaster raster = image.getRaster();
            int pixelRGBValue = raster.getSample(x, y, 0);
            return pixelRGBValue == 0;
        }

        int luminanceValue = 140;
        return isBlack(image, x, y, luminanceValue);
    }

    /**
     * Return true if a pixel is black.
     *
     * @param image Image
     * @param x X
     * @param y Y
     * @param luminanceCutOff Luminance cutoff
     * @return True if black
     */
    private static boolean isBlack(BufferedImage image, int x, int y, int luminanceCutOff) {
        int pixelRGBValue;
        int r;
        int g;
        int b;
        double luminance = 0.0;

        // return white on areas outside of image boundaries
        if (x < 0 || y < 0 || x > image.getWidth() || y > image.getHeight()) {
            return false;
        }

        try {
            pixelRGBValue = image.getRGB(x, y);
            r = (pixelRGBValue >> 16) & 0xff;
            g = (pixelRGBValue >> 8) & 0xff;
            b = (pixelRGBValue) & 0xff;
            luminance = (r * 0.299) + (g * 0.587) + (b * 0.114);
        } catch (Exception e) {
            // NOP
        }

        return luminance < luminanceCutOff;
    }
}
