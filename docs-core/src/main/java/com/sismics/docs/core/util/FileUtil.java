package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.tess4j.Tesseract;
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
     * @param pdfInputStream Unencrypted PDF input stream
     * @return Content extract
     */
    public static String extractContent(Document document, File file, InputStream inputStream, InputStream pdfInputStream) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(inputStream, document);
        } else if (pdfInputStream != null) {
            content = extractPdf(pdfInputStream);
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
        } catch (Throwable e) {
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
            pdfDocument = PDDocument.load(inputStream);
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
     * Convert a file to PDF if necessary.
     * 
     * @param inputStream InputStream
     * @param file File
     * @return PDF input stream
     * @throws Exception 
     */
    public static InputStream convertToPdf(InputStream inputStream, File file) throws Exception {
        if (file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            // It's already PDF, just return the input
            return inputStream;
        }
        
        if (file.getMimeType().equals(MimeType.OFFICE_DOCUMENT)) {
            return convertOfficeDocument(inputStream);
        }
        
        if (file.getMimeType().equals(MimeType.OPEN_DOCUMENT_TEXT)) {
            return convertOpenDocumentText(inputStream);
        }
        
        // PDF conversion not necessary/possible
        return null;
    }
    
    /**
     * Convert an open document text file to PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @return PDF input stream
     * @throws Exception 
     */
    private static InputStream convertOpenDocumentText(InputStream inputStream) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        OdfTextDocument document = OdfTextDocument.loadDocument(inputStream);
        PdfOptions options = PdfOptions.create();
        PdfConverter.getInstance().convert(document, pdfOutputStream, options);
        inputStream.reset();
        return new ByteArrayInputStream(pdfOutputStream.toByteArray());
    }
    
    /**
     * Convert an Office document to PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @return PDF input stream
     * @throws Exception 
     */
    private static InputStream convertOfficeDocument(InputStream inputStream) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        XWPFDocument document = new XWPFDocument(inputStream);
        org.apache.poi.xwpf.converter.pdf.PdfOptions options = org.apache.poi.xwpf.converter.pdf.PdfOptions.create();
        org.apache.poi.xwpf.converter.pdf.PdfConverter.getInstance().convert(document, pdfOutputStream, options);
        inputStream.reset();
        return new ByteArrayInputStream(pdfOutputStream.toByteArray());
    }
    
    /**
     * Save a file on the storage filesystem.
     * 
     * @param inputStream Unencrypted input stream
     * @param pdf
     * @param file File to save
     * @param privateKey Private key used for encryption
     * @throws Exception
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
     * @throws Exception
     */
    public static void saveVariations(File file, InputStream inputStream, InputStream pdfInputStream, Cipher cipher) throws Exception {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            image = ImageIO.read(inputStream);
            inputStream.reset();
        } else if(pdfInputStream != null) {
            // Generate preview from the first page of the PDF
            PDDocument pdfDocument = null;
            try {
                pdfDocument = PDDocument.load(pdfInputStream);
                PDFRenderer renderer = new PDFRenderer(pdfDocument);
                image = renderer.renderImage(0);
                pdfInputStream.reset();
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
     * @throws IOException 
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
