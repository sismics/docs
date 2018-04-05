package com.sismics.docs.core.util.format;

import com.google.common.io.Closer;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.util.mime.MimeType;
import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ODT format handler.
 *
 * @author bgamard
 */
public class OdtFormatHandler implements FormatHandler {
    /**
     * Temporary PDF file.
     */
    private Path temporaryPdfFile;

    @Override
    public boolean accept(String mimeType) {
        return MimeType.OPEN_DOCUMENT_TEXT.equals(mimeType);
    }

    @Override
    public BufferedImage generateThumbnail(Path file) throws Exception {
        // Use the PDF format handler
        return new PdfFormatHandler().generateThumbnail(getGeneratedPdf(file));
    }

    @Override
    public String extractContent(String language, Path file) throws Exception {
        // Use the PDF format handler
        return new PdfFormatHandler().extractContent(language, getGeneratedPdf(file));
    }

    @Override
    public void appendToPdf(Path file, PDDocument doc, boolean fitImageToPage, int margin, MemoryUsageSetting memUsageSettings, Closer closer) throws Exception {
        // Use the PDF format handler
        new PdfFormatHandler().appendToPdf(getGeneratedPdf(file), doc, fitImageToPage, margin, memUsageSettings, closer);
    }

    /**
     * Generate a PDF from this ODT.
     *
     * @param file File
     * @return PDF file
     * @throws Exception e
     */
    private Path getGeneratedPdf(Path file) throws Exception {
        if (temporaryPdfFile == null) {
            temporaryPdfFile = AppContext.getInstance().getFileService().createTemporaryFile();
            try (InputStream inputStream = Files.newInputStream(file);
                 OutputStream outputStream = Files.newOutputStream(temporaryPdfFile)) {
                OdfTextDocument document = OdfTextDocument.loadDocument(inputStream);
                PdfOptions options = PdfOptions.create();
                PdfConverter.getInstance().convert(document, outputStream, options);
            }
        }

        return temporaryPdfFile;
    }
}
