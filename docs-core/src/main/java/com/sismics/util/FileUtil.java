package com.sismics.util;

import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File utilities.
 * 
 * @author bgamard
 */
public class FileUtil {

    /**
     * Save a file on the storage filesystem.
     * 
     * @param is InputStream
     * @param file File to save
     * @throws Exception
     */
    public static void save(InputStream is, File file) throws Exception {
        Path path = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId());
        Files.copy(is, path);
        
        // In case of image, save a thumbnail
        if (ImageUtil.isImage(file.getMimeType())) {
            BufferedImage image = ImageIO.read(path.toFile());
            BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 256, Scalr.OP_ANTIALIAS);
            image.flush();
            ImageUtil.writeJpeg(resizedImage, Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_thumb").toFile());
        }
    }
}
