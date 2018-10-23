package com.sismics.docs.core.util.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.Closeable;
import java.io.IOException;

/**
 * Wrapper around PDFBox for high level abstraction of PDF writing.
 * 
 * @author bgamard
 */
public class PdfPage implements Closeable {
    private PDPage pdPage;
    private PDPageContentStream pdContent;
    private float margin;
    private PDFont defaultFont;
    private int defaultFontSize;

    /**
     * Create a wrapper around a PDF page.
     * 
     * @param pdDoc Document
     * @param pdPage Page
     * @param margin Margin
     * @param defaultFont Default font
     * @param defaultFontSize Default fond size
     * @throws IOException e
     */
    public PdfPage(PDDocument pdDoc, PDPage pdPage, float margin, PDFont defaultFont, int defaultFontSize) throws IOException {
        this.pdPage = pdPage;
        this.pdContent = new PDPageContentStream(pdDoc, pdPage);
        this.margin = margin;
        this.defaultFont = defaultFont;
        this.defaultFontSize = defaultFontSize;
        
        pdContent.beginText();
        pdContent.newLineAtOffset(margin, pdPage.getMediaBox().getHeight() - margin);
    }
    
    /**
     * Write a text with default font.
     * 
     * @param text Text
     * @throws IOException e
     */
    public PdfPage addText(String text) throws IOException {
        drawText(pdPage.getMediaBox().getWidth() - 2 * margin, defaultFont, defaultFontSize, text, false);
        return this;
    }
    
    /**
     * Write a text with default font.
     * 
     * @param text Text
     * @param centered If true, the text will be centered in the page
     * @throws IOException e
     */
    public PdfPage addText(String text, boolean centered) throws IOException {
        drawText(pdPage.getMediaBox().getWidth() - 2 * margin, defaultFont, defaultFontSize, text, centered);
        return this;
    }
    
    /**
     * Write a text in the page.
     * 
     * @param text Text
     * @param centered If true, the text will be centered in the page
     * @param font Font
     * @param fontSize Font size
     * @throws IOException e
     */
    public PdfPage addText(String text, boolean centered, PDFont font, int fontSize) throws IOException {
        drawText(pdPage.getMediaBox().getWidth() - 2 * margin, font, fontSize, text, centered);
        return this;
    }
    
    /**
     * Create a new line.
     * 
     * @throws IOException e
     */
    public PdfPage newLine() throws IOException {
        pdContent.newLineAtOffset(0, - defaultFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * defaultFontSize);
        return this;
    }
    
    /**
     * Draw a text with low level PDFBox API.
     * 
     * @param paragraphWidth Paragraph width
     * @param font Font
     * @param fontSize Font size
     * @param text Text
     * @param centered If true, the text will be centered in the paragraph
     * @throws IOException e
     */
    private void drawText(float paragraphWidth, PDFont font, int fontSize, String text, boolean centered) throws IOException {
        if (text == null) {
            return;
        }

        pdContent.setFont(font, fontSize);
        int start = 0;
        int end = 0;
        float height = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        for (int i : possibleWrapPoints(text)) {
            float width = font.getStringWidth(text.substring(start, i)) / 1000 * fontSize;
            if (start < end && width > paragraphWidth) {
                // Draw partial text and increase height
                pdContent.newLineAtOffset(0, - height);
                String line = text.substring(start, end);
                float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
                float offset = (paragraphWidth - lineWidth) / 2;
                if (centered) pdContent.newLineAtOffset(offset, 0);
                pdContent.showText(line);
                if (centered) pdContent.newLineAtOffset(- offset, 0);
                start = end;
            }
            end = i;
        }
        
        // Last piece of text
        String line = text.substring(start);
        float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
        float offset = (paragraphWidth - lineWidth) / 2;
        pdContent.newLineAtOffset(0, - height);
        if (centered) pdContent.newLineAtOffset(offset, 0);
        pdContent.showText(line);
        if (centered) pdContent.newLineAtOffset(- offset, 0);
    }

    /**
     * Returns wrap points for a given piece of text.
     * 
     * @param text Text
     * @return Wrap points
     */
    private int[] possibleWrapPoints(String text) {
        String[] split = text.split("(?<=\\W)");
        int[] ret = new int[split.length];
        ret[0] = split[0].length();
        for (int i = 1 ; i < split.length ; i++) {
            ret[i] = ret[i-1] + split[i].length();
        }
        return ret;
    }

    @Override
    public void close() throws IOException {
        pdContent.endText();
        pdContent.close();
    }
}
