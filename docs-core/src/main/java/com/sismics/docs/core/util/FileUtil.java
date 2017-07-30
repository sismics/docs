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
     * @param inputStream Unencrypted input stream
     * @param pdfInputStream Unencrypted PDF input stream
     * @return Content extract
     */
    public static String extractContent(String language, File file, InputStream inputStream, InputStream pdfInputStream) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(inputStream, language);
        } else if (pdfInputStream != null) {
            content = PdfUtil.extractPdf(pdfInputStream);
        }
        
        return content;
    }
    
    /**
     * Optical character recognition on a stream.
     * 
     * @param inputStream Unencrypted input stream
     * @param language Language to OCR
     * @return Content extracted
     */
    private static String ocrFile(InputStream inputStream, String language) {
        Tesseract instance = Tesseract.getInstance();
        String content = null;
        BufferedImage image;
        try {
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
     * @param inputStream Unencrypted input stream
     * @param pdfInputStream PDF input stream
     * @param file File to save
     * @param privateKey Private key used for encryption
     */
    public static void save(InputStream inputStream, InputStream pdfInputStream, File file, String privateKey) throws Exception {
        Cipher cipher = EncryptionUtil.getEncryptionCipher(privateKey);
        Path path = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        Files.copy(new CipherInputStream(inputStream, cipher), path);
        inputStream.reset();
        
        // Generate file variations
        saveVariations(file, inputStream, pdfInputStream, cipher);
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param inputStream Unencrypted input stream
     * @param pdfInputStream Unencrypted PDF input stream
     * @param cipher Cipher to use for encryption
     */
    private static void saveVariations(File file, InputStream inputStream, InputStream pdfInputStream, Cipher cipher) throws Exception {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            image = ImageIO.read(inputStream);
            inputStream.reset();
        } else if(pdfInputStream != null) {
            // Generate preview from the first page of the PDF
            image = PdfUtil.renderFirstPage(pdfInputStream);
            pdfInputStream.reset();
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
