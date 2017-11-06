package com.sismics.util;

import com.sismics.docs.core.model.jpa.File;
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
        File file = new File();
        file.setMimeType(MimeType.APPLICATION_ZIP);
        Assert.assertEquals(MimeType.OPEN_DOCUMENT_TEXT, MimeTypeUtil.guessOpenDocumentFormat(file, path));

        // Detect DOCX files
        path = Paths.get(ClassLoader.getSystemResource("file/document.docx").toURI());
        file = new File();
        file.setMimeType(MimeType.APPLICATION_ZIP);
        Assert.assertEquals(MimeType.OFFICE_DOCUMENT, MimeTypeUtil.guessOpenDocumentFormat(file, path));
    }
}
