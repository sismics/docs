package com.sismics.util;

import com.sismics.util.mime.MimeType;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
     * @param file
     * @throws IOException
     */
    public static void writeJpeg(BufferedImage image, File file) throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = null;
        FileImageOutputStream output = null;
        try {
            writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(1.f);
            output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage iioImage = new IIOImage(image, null, null);
            writer.write(null, iioImage, iwp);
        } finally {
            if (output != null) {
                try {
                    output.close();
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
}
