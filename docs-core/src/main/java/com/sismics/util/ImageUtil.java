package com.sismics.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.sismics.util.mime.MimeType;

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
            writer = (ImageWriter) iter.next();
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
     * @param email
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
}
