package com.sismics.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;

/**
 * Test of the utilities to check MIME types.
 * 
 * @author bgamard
 */
public class TestMimeTypeUtil {

    @Test
    public void guessOpenDocumentFormatTest() throws Exception {
        // Detect ODT files
        try (InputStream inputStream = Resources.getResource("file/document.odt").openStream();
                InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            File file = new File();
            file.setMimeType(MimeType.APPLICATION_ZIP);
            Assert.assertEquals(MimeType.OPEN_DOCUMENT_TEXT, MimeTypeUtil.guessOpenDocumentFormat(file, byteArrayInputStream));
        }
        
        // Detect DOCX files
        try (InputStream inputStream = Resources.getResource("file/document.docx").openStream();
                InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            File file = new File();
            file.setMimeType(MimeType.APPLICATION_ZIP);
            Assert.assertEquals(MimeType.OFFICE_DOCUMENT, MimeTypeUtil.guessOpenDocumentFormat(file, byteArrayInputStream));
        }
    }
}
