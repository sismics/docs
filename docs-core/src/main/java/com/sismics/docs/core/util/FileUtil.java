package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.ImageUtil;
import com.sismics.util.mime.MimeType;

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
     * @param document Document linked to the file
     * @param file File to extract
     * @param inputStream Unencrypted input stream
     * @return Content extract
     */
    public static String extractContent(Document document, File file, InputStream inputStream) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(inputStream, document);
        } else if (file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            content = extractPdf(inputStream);
        }
        
        return content;
    }
    
    /**
     * Optical character recognition on a stream.
     * 
     * @param inputStream Unencrypted input stream
     * @param document Document linked to the file
     * @return Content extracted
     */
    private static String ocrFile(InputStream inputStream, Document document) {
        Tesseract instance = Tesseract.getInstance();
        String content = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("Error reading the image", e);
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
            log.error("Error while OCR-izing the image", e);
        }
        
        return content;
    }
    
    /**
     * Extract text from a PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @return Content extracted
     */
    private static String extractPdf(InputStream inputStream) {
        String content = null;
        PDDocument pdfDocument = null;
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfDocument = PDDocument.load(inputStream, true);
            content = stripper.getText(pdfDocument);
        } catch (IOException e) {
            log.error("Error while extracting text from the PDF", e);
        } finally {
            if (pdfDocument != null) {
                try {
                    pdfDocument.close();
                } catch (IOException e) {
                    // NOP
                }
            }
        }
        
        return content;
    }
    
    /**
     * Save a file on the storage filesystem.
     * 
     * @param inputStream Unencrypted input stream
     * @param file File to save
     * @param privateKey Private key used for encryption
     * @throws Exception
     */
    public static void save(InputStream inputStream, File file, String privateKey) throws Exception {
        Cipher cipher = EncryptionUtil.getEncryptionCipher(privateKey);
        Path path = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId());
        Files.copy(new CipherInputStream(inputStream, cipher), path);
        
        // Generate file variations
        inputStream.reset();
        saveVariations(file, inputStream, cipher);
        inputStream.reset();
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param inputStream Unencrypted input stream
     * @param cipher Cipher to use for encryption
     * @throws Exception
     */
    public static void saveVariations(File file, InputStream inputStream, Cipher cipher) throws Exception {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            image = ImageIO.read(inputStream);
        } else if(file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            // Generate preview from the first page of the PDF
            PDDocument pdfDocument = null;
            try {
                pdfDocument = PDDocument.load(inputStream, true);
                @SuppressWarnings("unchecked")
                List<PDPage> pageList = pdfDocument.getDocumentCatalog().getAllPages();
                if (pageList.size() > 0) {
                    PDPage page = pageList.get(0);
                    image = page.convertToImage();
                }
            } finally {
                pdfDocument.close();
            }
        }
        
        if (image != null) {
            // Generate thumbnails from image
            BufferedImage web = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 1280, Scalr.OP_ANTIALIAS);
            BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 256, Scalr.OP_ANTIALIAS);
            image.flush();
            
            // Write "web" encrypted image
            java.io.File outputFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_web").toFile();
            OutputStream outputStream = new CipherOutputStream(new FileOutputStream(outputFile), cipher);
            try {
                ImageUtil.writeJpeg(web, outputStream);
            } finally {
                outputStream.close();
            }
            
            // Write "thumb" encrypted image
            outputFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_thumb").toFile();
            outputStream = new CipherOutputStream(new FileOutputStream(outputFile), cipher);
            try {
                ImageUtil.writeJpeg(thumbnail, outputStream);
            } finally {
                outputStream.close();
            }
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
