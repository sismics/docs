package com.sismics.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.sismics.util.mime.MimeType;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
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
     * @param image
     * @param outputStream Output stream
     * @throws IOException
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
     * Returns true if this MIME type is an image.
     * @param mimeType MIME type
     * @return True if image
     */
    public static boolean isImage(String mimeType) {
        return mimeType.equals(MimeType.IMAGE_GIF) || mimeType.equals(MimeType.IMAGE_PNG) || mimeType.equals(MimeType.IMAGE_JPEG);
    }
    
    /**
     * Compute Gravatar hash.
     * See https://en.gravatar.com/site/implement/hash/.
     * 
     * @param email Email
     * @return Gravatar hash
     */
    public static String computeGravatar(String email) {
        if (email == null) {
            return null;
        }
        
        return Hashing.md5().hashString(
                email.trim().toLowerCase(), Charsets.UTF_8)
                .toString();
    }

    public static boolean isBlack(BufferedImage image, int x, int y) {
        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
            WritableRaster raster = image.getRaster();
            int pixelRGBValue = raster.getSample(x, y, 0);
            return pixelRGBValue == 0;
        }

        int luminanceValue = 140;
        return isBlack(image, x, y, luminanceValue);
    }

    public static boolean isBlack(BufferedImage image, int x, int y, int luminanceCutOff) {
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
            // ignore.
        }

        return luminance < luminanceCutOff;
    }
}
