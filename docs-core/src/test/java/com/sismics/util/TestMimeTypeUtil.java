package com.sismics.util;

import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test of the utilities to check MIME types.
 * 
 * @author bgamard
 */
public class TestMimeTypeUtil {
    @Test
    public void test() throws Exception {
        // Detect ODT files
        Path path = Paths.get(ClassLoader.getSystemResource("file/document.odt").toURI());
        Assert.assertEquals(MimeType.OPEN_DOCUMENT_TEXT, MimeTypeUtil.guessMimeType(path, "document.odt"));

        // Detect DOCX files
        path = Paths.get(ClassLoader.getSystemResource("file/document.docx").toURI());
        Assert.assertEquals(MimeType.OFFICE_DOCUMENT, MimeTypeUtil.guessMimeType(path, "document.odt"));

        // Detect PPTX files
        path = Paths.get(ClassLoader.getSystemResource("file/apache.pptx").toURI());
        Assert.assertEquals(MimeType.OFFICE_PRESENTATION, MimeTypeUtil.guessMimeType(path, "apache.pptx"));

        // Detect XLSX files
        path = Paths.get(ClassLoader.getSystemResource("file/document.xlsx").toURI());
        Assert.assertEquals(MimeType.OFFICE_SHEET, MimeTypeUtil.guessMimeType(path, "document.xlsx"));

        // Detect TXT files
        path = Paths.get(ClassLoader.getSystemResource("file/document.txt").toURI());
        Assert.assertEquals(MimeType.TEXT_PLAIN, MimeTypeUtil.guessMimeType(path, "document.txt"));

        // Detect CSV files
        path = Paths.get(ClassLoader.getSystemResource("file/document.csv").toURI());
        Assert.assertEquals(MimeType.TEXT_CSV, MimeTypeUtil.guessMimeType(path, "document.csv"));

        // Detect PDF files
        path = Paths.get(ClassLoader.getSystemResource("file/udhr.pdf").toURI());
        Assert.assertEquals(MimeType.APPLICATION_PDF, MimeTypeUtil.guessMimeType(path, "udhr.pdf"));

        // Detect JPEG files
        path = Paths.get(ClassLoader.getSystemResource("file/apollo_portrait.jpg").toURI());
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(path, "apollo_portrait.jpg"));

        // Detect GIF files
        path = Paths.get(ClassLoader.getSystemResource("file/image.gif").toURI());
        Assert.assertEquals(MimeType.IMAGE_GIF, MimeTypeUtil.guessMimeType(path, "image.gif"));

        // Detect PNG files
        path = Paths.get(ClassLoader.getSystemResource("file/image.png").toURI());
        Assert.assertEquals(MimeType.IMAGE_PNG, MimeTypeUtil.guessMimeType(path, "image.png"));

        // Detect ZIP files
        path = Paths.get(ClassLoader.getSystemResource("file/document.zip").toURI());
        Assert.assertEquals(MimeType.APPLICATION_ZIP, MimeTypeUtil.guessMimeType(path, "document.zip"));

        // Detect WEBM files
        path = Paths.get(ClassLoader.getSystemResource("file/video.webm").toURI());
        Assert.assertEquals(MimeType.VIDEO_WEBM, MimeTypeUtil.guessMimeType(path, "video.webm"));

        // Detect MP4 files
        path = Paths.get(ClassLoader.getSystemResource("file/video.mp4").toURI());
        Assert.assertEquals(MimeType.VIDEO_MP4, MimeTypeUtil.guessMimeType(path, "video.mp4"));
    }
}
