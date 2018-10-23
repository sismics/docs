package com.sismics.docs.core.util.format;

import com.google.common.io.Closer;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * A format handler.
 *
 * @author bgamard
 */
public interface FormatHandler {
    /**
     * Returns true if this format handler can handle this MIME type.
     *
     * @param mimeType MIME type
     * @return True if accepted
     */
    boolean accept(String mimeType);

    /**
     * Generate a thumbnail.
     *
     * @param file File
     * @return Thumbnail
     * @throws Exception e
     */
    BufferedImage generateThumbnail(Path file) throws Exception;

    /**
     * Extract text content.
     *
     * @param language Language
     * @param file File
     * @return Text content
     * @throws Exception e
     */
    String extractContent(String language, Path file) throws Exception;

    /**
     * Append to a PDF.
     *
     * @param file File
     * @param doc PDF document
     * @param fitImageToPage Fit image to page
     * @param margin Margin
     * @param memUsageSettings Memory usage
     * @param closer Closer
     * @throws Exception e
     */
    void appendToPdf(Path file, PDDocument doc, boolean fitImageToPage, int margin, MemoryUsageSetting memUsageSettings, Closer closer) throws Exception;
}
