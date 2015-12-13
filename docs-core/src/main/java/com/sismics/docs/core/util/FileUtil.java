package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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

import com.google.common.io.Closer;
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
     * @param file File
     * @param inputStream InputStream
     * @param reset Reset the stream after usage
     * @return PDF input stream
     * @throws Exception 
     */
    public static InputStream convertToPdf(File file, InputStream inputStream, boolean reset) throws Exception {
        if (file.getMimeType().equals(MimeType.APPLICATION_PDF)) {
            // It's already PDF, just return the input
            return inputStream;
        }
        
        if (file.getMimeType().equals(MimeType.OFFICE_DOCUMENT)) {
            return convertOfficeDocument(inputStream, reset);
        }
        
        if (file.getMimeType().equals(MimeType.OPEN_DOCUMENT_TEXT)) {
            return convertOpenDocumentText(inputStream, reset);
        }
        
        // PDF conversion not necessary/possible
        return null;
    }
    
    /**
     * Convert an open document text file to PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @param reset Reset the stream after usage
     * @return PDF input stream
     * @throws Exception 
     */
    private static InputStream convertOpenDocumentText(InputStream inputStream, boolean reset) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        OdfTextDocument document = OdfTextDocument.loadDocument(inputStream);
        PdfOptions options = PdfOptions.create();
        PdfConverter.getInstance().convert(document, pdfOutputStream, options);
        if (reset) {
            inputStream.reset();
        }
        return new ByteArrayInputStream(pdfOutputStream.toByteArray());
    }
    
    /**
     * Convert an Office document to PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @param reset Reset the stream after usage
     * @return PDF input stream
     * @throws Exception 
     */
    private static InputStream convertOfficeDocument(InputStream inputStream, boolean reset) throws Exception {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        XWPFDocument document = new XWPFDocument(inputStream);
        org.apache.poi.xwpf.converter.pdf.PdfOptions options = org.apache.poi.xwpf.converter.pdf.PdfOptions.create();
        org.apache.poi.xwpf.converter.pdf.PdfConverter.getInstance().convert(document, pdfOutputStream, options);
        if (reset) {
            inputStream.reset();
        }
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
    
    /**
     * Convert a document and its files to a merged PDF file.
     * 
     * @param fileList List of files
     * @param fitImageToPage Fill images to the page
     * @param margin Margins in millimeters
     * @return PDF input stream
     * @throws IOException 
     */
    public static InputStream convertToPdf(List<File> fileList, boolean fitImageToPage, int margin) throws Exception {
        // TODO PDF Export: Option to add a front page with:
        // document title, document description, creator, date created, language,
        // list of all files (and information if it is in this document or not)
        // TODO PDF Export: Option to add the comments
        
        // Create a blank PDF
        Closer closer = Closer.create();
        MemoryUsageSetting memUsageSettings = MemoryUsageSetting.setupMixed(1000000); // 1MB max memory usage
        memUsageSettings.setTempDir(new java.io.File(System.getProperty("java.io.tmpdir"))); // To OS temp
        float mmPerInch = 1 / (10 * 2.54f) * 72f;
        
        try (PDDocument doc = new PDDocument(memUsageSettings)) {
            // Add files
            for (File file : fileList) {
                Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
                try (InputStream storedFileInputStream = file.getPrivateKey() == null ? // Try to decrypt the file if we have a private key available
                        Files.newInputStream(storedFile) : EncryptionUtil.decryptInputStream(Files.newInputStream(storedFile), file.getPrivateKey())) {
                    if (ImageUtil.isImage(file.getMimeType())) {
                        PDPage page = new PDPage(PDRectangle.A4); // Images into A4 pages
                        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                            // Read the image using the correct handler. PDFBox can't do it because it relies wrongly on file extension
                            PDImageXObject pdImage = null;
                            if (file.getMimeType().equals(MimeType.IMAGE_JPEG)) {
                                pdImage = JPEGFactory.createFromStream(doc, storedFileInputStream);
                            } else if (file.getMimeType().equals(MimeType.IMAGE_GIF) || file.getMimeType().equals(MimeType.IMAGE_PNG)) {
                                BufferedImage bim = ImageIO.read(storedFileInputStream);
                                pdImage = LosslessFactory.createFromImage(doc, bim);
                            }
                            
                            if (fitImageToPage) {
                                // Fill the page with the image
                                float widthAvailable = page.getMediaBox().getWidth() - 2 * margin * mmPerInch;
                                float heightAvailable = page.getMediaBox().getHeight() - 2 * margin * mmPerInch;
                                
                                // Compare page format and image format
                                if (widthAvailable / heightAvailable < (float) pdImage.getWidth() / (float) pdImage.getHeight()) {
                                    float imageHeight = widthAvailable / pdImage.getWidth() * pdImage.getHeight();
                                    contentStream.drawImage(pdImage, margin * mmPerInch, heightAvailable + margin * mmPerInch - imageHeight,
                                            widthAvailable, imageHeight);
                                } else {
                                    float imageWidth = heightAvailable / pdImage.getHeight() * pdImage.getWidth();
                                    contentStream.drawImage(pdImage, margin * mmPerInch, margin * mmPerInch,
                                            imageWidth, heightAvailable);
                                }
                            } else {
                                // Draw the image as is
                                contentStream.drawImage(pdImage, margin * mmPerInch,
                                        page.getMediaBox().getHeight() - pdImage.getHeight() - margin * mmPerInch);
                            }
                        }
                        doc.addPage(page);
                    } else {
                        // Try to convert the file to PDF
                        InputStream pdfInputStream = convertToPdf(file, storedFileInputStream, false);
                        if (pdfInputStream != null) {
                            // This file is convertible to PDF, just add it to the end
                            try {
                                PDDocument mergeDoc = PDDocument.load(pdfInputStream, memUsageSettings);
                                closer.register(mergeDoc);
                                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                                pdfMergerUtility.appendDocument(doc, mergeDoc);
                            } finally {
                                pdfInputStream.close();
                            }
                        }
                        
                        // All other non-PDF-convertible files are ignored
                    }
                }
            }
            
            // Save to a temporary file
            try (TemporaryFileStream temporaryFileStream = new TemporaryFileStream()) {
                doc.save(temporaryFileStream.openWriteStream());
                closer.close(); // Close all remaining opened PDF
                return temporaryFileStream.openReadStream();
            }
        }
    }
}
