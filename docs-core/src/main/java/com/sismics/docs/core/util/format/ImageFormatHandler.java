package com.sismics.docs.core.util.format;

import com.google.common.io.Closer;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.util.mime.MimeType;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Image format handler.
 *
 * @author bgamard
 */
public class ImageFormatHandler implements FormatHandler {
    /**
     * Saved MIME type.
     */
    private String mimeType;

    @Override
    public boolean accept(String mimeType) {
        this.mimeType = mimeType;
        return mimeType.equals(MimeType.IMAGE_GIF) || mimeType.equals(MimeType.IMAGE_PNG) || mimeType.equals(MimeType.IMAGE_JPEG);
    }

    @Override
    public BufferedImage generateThumbnail(Path file) throws Exception {
        try (InputStream inputStream = Files.newInputStream(file)) {
            return ImageIO.read(inputStream);
        }
    }

    @Override
    public String extractContent(String language, Path file) throws Exception {
        if (language == null) {
            return null;
        }

        try (InputStream inputStream = Files.newInputStream(file)) {
            return FileUtil.ocrFile(language, ImageIO.read(inputStream));
        }
    }

    @Override
    public void appendToPdf(Path file, PDDocument doc, boolean fitImageToPage, int margin, MemoryUsageSetting memUsageSettings, Closer closer) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4); // Images into A4 pages
        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page);
             InputStream storedFileInputStream = Files.newInputStream(file)) {
            // Read the image using the correct handler. PDFBox can't do it because it relies wrongly on file extension
            PDImageXObject pdImage;
            switch (mimeType) {
                case MimeType.IMAGE_JPEG:
                    pdImage = JPEGFactory.createFromStream(doc, storedFileInputStream);
                    break;
                case MimeType.IMAGE_GIF:
                case MimeType.IMAGE_PNG:
                    BufferedImage bim = ImageIO.read(storedFileInputStream);
                    pdImage = LosslessFactory.createFromImage(doc, bim);
                    break;
                default:
                    return;
            }

            // Do we want to fill the page with the image?
            if (fitImageToPage) {
                // Fill the page with the image
                float widthAvailable = page.getMediaBox().getWidth() - 2 * margin * Constants.MM_PER_INCH;
                float heightAvailable = page.getMediaBox().getHeight() - 2 * margin * Constants.MM_PER_INCH;

                // Compare page format and image format
                if (widthAvailable / heightAvailable < (float) pdImage.getWidth() / (float) pdImage.getHeight()) {
                    float imageHeight = widthAvailable / pdImage.getWidth() * pdImage.getHeight();
                    contentStream.drawImage(pdImage, margin * Constants.MM_PER_INCH, heightAvailable + margin * Constants.MM_PER_INCH - imageHeight,
                            widthAvailable, imageHeight);
                } else {
                    float imageWidth = heightAvailable / pdImage.getHeight() * pdImage.getWidth();
                    contentStream.drawImage(pdImage, margin * Constants.MM_PER_INCH, margin * Constants.MM_PER_INCH,
                            imageWidth, heightAvailable);
                }
            } else {
                // Draw the image as is
                contentStream.drawImage(pdImage, margin * Constants.MM_PER_INCH,
                        page.getMediaBox().getHeight() - pdImage.getHeight() - margin * Constants.MM_PER_INCH);
            }
        }
        doc.addPage(page);
    }
}
