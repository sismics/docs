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
    public void guessOpenDocumentFormatTest() throws Exception {
        // Detect ODT files
        Path path = Paths.get(ClassLoader.getSystemResource("file/document.odt").toURI());
        Assert.assertEquals(MimeType.OPEN_DOCUMENT_TEXT, MimeTypeUtil.guessMimeType(path, "document.odt"));

        // Detect DOCX files
        path = Paths.get(ClassLoader.getSystemResource("file/document.docx").toURI());
        Assert.assertEquals(MimeType.OFFICE_DOCUMENT, MimeTypeUtil.guessMimeType(path, "document.odt"));
    }
}
