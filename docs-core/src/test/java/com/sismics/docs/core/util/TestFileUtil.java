package com.sismics.docs.core.util;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.Resources;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.mime.MimeType;

/**
 * Test of the file entity utilities.
 * 
 * @author bgamard
 */
public class TestFileUtil {
    @Test
    public void extractContentOpenDocumentTextTest() throws Exception {
        try (InputStream inputStream = Resources.getResource("file/document.odt").openStream()) {
            File file = new File();
            file.setMimeType(MimeType.OPEN_DOCUMENT_TEXT);
            Assert.assertEquals("Lorem ipsum dolor sit amen.\r\n", FileUtil.extractContent(null, file, inputStream));
        }
    }
    
    @Test
    public void extractContentOfficeDocumentTest() throws Exception {
        try (InputStream inputStream = Resources.getResource("file/document.docx").openStream()) {
            File file = new File();
            file.setMimeType(MimeType.OFFICE_DOCUMENT);
            Assert.assertEquals("Lorem ipsum dolor sit amen.\r\n", FileUtil.extractContent(null, file, inputStream));
        }
    }
}
