package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.ImageUtil;

/**
 * File entity utilities.
 * 
 * @author bgamard
 */
public class FileUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    
    /**
     * OCR a file.
     * 
     * @param document Document linked to the file
     * @param file File to OCR
     * @return OCR-ized content
     */
    public static String ocrFile(Document document, final File file) {
        if (!ImageUtil.isImage(file.getMimeType())) {
            // The file is not OCR-izable
            return null;
        }
        
        Tesseract instance = Tesseract.getInstance();
        java.io.File storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
        String content = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(storedfile);
        } catch (IOException e) {
            log.error("Error reading the image " + storedfile, e);
        }
        
        // Upscale and grayscale the image
        BufferedImage resizedImage = Scalr.resize(image, Method.AUTOMATIC, Mode.AUTOMATIC, 3500, Scalr.OP_ANTIALIAS, Scalr.OP_GRAYSCALE);
        image.flush();
        image = resizedImage;

        // OCR the file
        try {
            log.info("Starting OCR with TESSDATA_PREFIX=" + System.getenv("TESSDATA_PREFIX") + ";LC_NUMERIC=" + System.getenv("LC_NUMERIC"));
            instance.setLanguage(document.getLanguage());
            content = instance.doOCR(image);
        } catch (Exception e) {
            log.error("Error while OCR-izing the file " + storedfile, e);
        }
        
        return content;
    }
    
    /**
     * Save a file on the storage filesystem.
     * 
     * @param is InputStream
     * @param file File to save
     * @throws IOException
     */
    public static void save(InputStream is, File file) throws IOException {
        Path path = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId());
        Files.copy(is, path);
        
        // Generate file variations
        saveVariations(file, path.toFile());
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param originalFile Original file
     * @throws IOException
     */
    public static void saveVariations(File file, java.io.File originalFile) throws IOException {
        if (ImageUtil.isImage(file.getMimeType())) {
            BufferedImage image = ImageIO.read(originalFile);
            BufferedImage web = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 1280, Scalr.OP_ANTIALIAS);
            BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 256, Scalr.OP_ANTIALIAS);
            image.flush();
            ImageUtil.writeJpeg(web, Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_web").toFile());
            ImageUtil.writeJpeg(thumbnail, Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_thumb").toFile());
        }
    }

    /**
     * Remove a file from the storage filesystem.
     * 
     * @param file File to delete
     */
    public static void delete(File file) {
        java.io.File storedFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
        java.io.File webFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_web").toFile();
        java.io.File thumbnailFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_thumb").toFile();
        
        if (storedFile.exists()) {
            storedFile.delete();
        }
        if (webFile.exists()) {
            webFile.delete();
        }
        if (thumbnailFile.exists()) {
            thumbnailFile.delete();
        }
    }
}
