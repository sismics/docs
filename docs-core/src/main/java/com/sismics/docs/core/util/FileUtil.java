package com.sismics.docs.core.util;

import com.sismics.docs.core.model.jpa.File;
import com.sismics.tess4j.Tesseract;
import com.sismics.util.ImageUtil;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
     * Extract content from a file.
     * 
     * @param language Language to extract
     * @param file File to extract
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @return Content extract
     */
    public static String extractContent(String language, File file, Path unencryptedFile, Path unencryptedPdfFile) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(unencryptedFile, language);
        } else if (unencryptedPdfFile != null) {
            content = PdfUtil.extractPdf(unencryptedPdfFile);
        }
        
        return content;
    }
    
    /**
     * Optical character recognition on a file.
     * 
     * @param unecryptedFile Unencrypted file
     * @param language Language to OCR
     * @return Content extracted
     */
    private static String ocrFile(Path unecryptedFile, String language) {
        Tesseract instance = Tesseract.getInstance();
        String content = null;
        BufferedImage image;
        try (InputStream inputStream = Files.newInputStream(unecryptedFile)) {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("Error reading the image", e);
            return null;
        }
        
        // Upscale and grayscale the image
        BufferedImage resizedImage = Scalr.resize(image, Method.AUTOMATIC, Mode.AUTOMATIC, 3500, Scalr.OP_ANTIALIAS, Scalr.OP_GRAYSCALE);
        image.flush();
        image = resizedImage;

        // OCR the file
        try {
            log.info("Starting OCR with TESSDATA_PREFIX=" + System.getenv("TESSDATA_PREFIX") + ";LC_NUMERIC=" + System.getenv("LC_NUMERIC"));
            instance.setLanguage(language);
            content = instance.doOCR(image);
        } catch (Throwable e) {
            log.error("Error while OCR-izing the image", e);
        }
        
        return content;
    }
    
    /**
     * Save a file on the storage filesystem.
     * 
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @param file File to save
     * @param privateKey Private key used for encryption
     */
    public static void save(Path unencryptedFile, Path unencryptedPdfFile, File file, String privateKey) throws Exception {
        Cipher cipher = EncryptionUtil.getEncryptionCipher(privateKey);
        Path path = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
            Files.copy(new CipherInputStream(inputStream, cipher), path);
        }

        // Generate file variations
        saveVariations(file, unencryptedFile, unencryptedPdfFile, cipher);
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @param cipher Cipher to use for encryption
     */
    private static void saveVariations(File file, Path unencryptedFile, Path unencryptedPdfFile, Cipher cipher) throws Exception {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
                image = ImageIO.read(inputStream);
            }
        } else if (unencryptedPdfFile != null) {
            // Generate preview from the first page of the PDF
            image = PdfUtil.renderFirstPage(unencryptedPdfFile);
        }
        
        if (image != null) {
            // Generate thumbnails from image
            BufferedImage web = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 1280, Scalr.OP_ANTIALIAS);
            BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 256, Scalr.OP_ANTIALIAS);
            image.flush();
            
            // Write "web" encrypted image
            Path outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_web");
            try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                ImageUtil.writeJpeg(web, outputStream);
            }
            
            // Write "thumb" encrypted image
            outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_thumb");
            try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                ImageUtil.writeJpeg(thumbnail, outputStream);
            }
        }
    }

    /**
     * Remove a file from the storage filesystem.
     * 
     * @param file File to delete
     */
    public static void delete(File file) throws IOException {
        Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        Path webFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_web");
        Path thumbnailFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_thumb");
        
        if (Files.exists(storedFile)) {
            Files.delete(storedFile);
        }
        if (Files.exists(webFile)) {
            Files.delete(webFile);
        }
        if (Files.exists(thumbnailFile)) {
            Files.delete(thumbnailFile);
        }
    }
}
