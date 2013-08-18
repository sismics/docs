package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
     * @return Content extract
     */
    public static String extractContent(Document document, File file) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(document, file);
        } else if (file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            content = extractPdf(file);
        }
        
        return content;
    }
    
    /**
     * Optical character recognition on a file.
     * 
     * @param document Document linked to the file
     * @param file File to OCR
     * @return Content extracted
     */
    private static String ocrFile(Document document, File file) {
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
     * Extract text from a PDF.
     * 
     * @param file File to extract
     * @return Content extracted
     */
    private static String extractPdf(File file) {
        String content = null;
        PDDocument pdfDocument = null;
        java.io.File storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfDocument = PDDocument.load(storedfile);
            content = stripper.getText(pdfDocument);
        } catch (IOException e) {
            log.error("Error while extracting text from the PDF " + storedfile, e);
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
     * @param is InputStream
     * @param file File to save
     * @throws IOException
     */
    public static void save(InputStream is, File file) throws IOException {
        Path path = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId());
        Files.copy(is, path);
        
        // Generate file variations
        try {
            saveVariations(file, path.toFile());
        } catch (IOException e) {
            // Don't rethrow Exception from file variations generation
            log.error("Error creating file variations", e);
        }
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param originalFile Original file
     * @throws IOException
     */
    public static void saveVariations(File file, java.io.File originalFile) throws IOException {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            image = ImageIO.read(originalFile);
        } else if(file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            // Generate preview from the first page of the PDF
            PDDocument pdfDocument = PDDocument.load(originalFile);
            @SuppressWarnings("unchecked")
            List<PDPage> pageList = pdfDocument.getDocumentCatalog().getAllPages();
            if (pageList.size() > 0) {
                PDPage page = pageList.get(0);
                image = page.convertToImage();
            }
        }
        
        if (image != null) {
            // Generate thumbnails from image
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
