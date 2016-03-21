package com.sismics.docs.core.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Closer;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.pdf.PdfPage;
import com.sismics.util.ImageUtil;
import com.sismics.util.mime.MimeType;

/**
 * PDF utilities.
 * 
 * @author bgamard
 */
public class PdfUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PdfUtil.class);
    
    /**
     * Extract text from a PDF.
     * 
     * @param inputStream Unencrypted input stream
     * @return Content extracted
     */
    public static String extractPdf(InputStream inputStream) {
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
     * Convert a document and its files to a merged PDF file.
     * 
     * @param documentDto Document DTO
     * @param fileList List of files
     * @param fitImageToPage Fit images to the page
     * @param metadata Add a page with metadata
     * @param margin Margins in millimeters
     * @return PDF input stream
     * @throws IOException 
     */
    public static InputStream convertToPdf(DocumentDto documentDto, List<File> fileList,
            boolean fitImageToPage, boolean metadata, int margin) throws Exception {
        // Setup PDFBox
        Closer closer = Closer.create();
        MemoryUsageSetting memUsageSettings = MemoryUsageSetting.setupMixed(1000000); // 1MB max memory usage
        memUsageSettings.setTempDir(new java.io.File(System.getProperty("java.io.tmpdir"))); // To OS temp
        float mmPerInch = 1 / (10 * 2.54f) * 72f;
        
        // Create a blank PDF
        try (PDDocument doc = new PDDocument(memUsageSettings)) {
            // Add metadata
            if (metadata) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PdfPage pdfPage = new PdfPage(doc, page, margin * mmPerInch, PDType1Font.HELVETICA, 12)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    pdfPage.addText(documentDto.getTitle(), true, PDType1Font.HELVETICA_BOLD, 16)
                        .newLine()
                        .addText("Created by " + documentDto.getCreator()
                            + " on " + dateFormat.format(new Date(documentDto.getCreateTimestamp())), true)
                        .newLine()
                        .addText(documentDto.getDescription())
                        .newLine();
                    if (!Strings.isNullOrEmpty(documentDto.getSubject())) {
                        pdfPage.addText("Subject: " + documentDto.getSubject());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getIdentifier())) {
                        pdfPage.addText("Identifier: " + documentDto.getIdentifier());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getPublisher())) {
                        pdfPage.addText("Publisher: " + documentDto.getPublisher());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getFormat())) {
                        pdfPage.addText("Format: " + documentDto.getFormat());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getSource())) {
                        pdfPage.addText("Source: " + documentDto.getSource());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getType())) {
                        pdfPage.addText("Type: " + documentDto.getType());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getCoverage())) {
                        pdfPage.addText("Coverage: " + documentDto.getCoverage());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getRights())) {
                        pdfPage.addText("Rights: " + documentDto.getRights());
                    }
                    pdfPage.addText("Language: " + documentDto.getLanguage())
                        .newLine()
                        .addText("Files in this document : " + fileList.size(), false, PDType1Font.HELVETICA_BOLD, 12);
                }
            }
            
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
                            
                            // Do we want to fill the page with the image?
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

    /**
     * Render the first page of a PDF.
     * 
     * @param inputStream PDF document
     * @return Render of the first page
     * @throws IOException
     */
    public static BufferedImage renderFirstPage(InputStream inputStream) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            PDFRenderer renderer = new PDFRenderer(pdfDocument);
            return renderer.renderImage(0);
        }
    }
}
