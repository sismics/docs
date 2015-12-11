package com.sismics.docs.core.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.pdfbox.io.IOUtils;
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
        try (InputStream inputStream = Resources.getResource("file/document.odt").openStream();
                InputStream bytesInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            File file = new File();
            file.setMimeType(MimeType.OPEN_DOCUMENT_TEXT);
            try (InputStream pdfInputStream = FileUtil.convertToPdf(bytesInputStream, file)) {
                Assert.assertEquals("Lorem ipsum dolor sit amen.\r\n", FileUtil.extractContent(null, file, inputStream, pdfInputStream));
            }
        }
    }
    
    @Test
    public void extractContentOfficeDocumentTest() throws Exception {
        try (InputStream inputStream = Resources.getResource("file/document.docx").openStream();
                InputStream bytesInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            File file = new File();
            file.setMimeType(MimeType.OFFICE_DOCUMENT);
            try (InputStream pdfInputStream = FileUtil.convertToPdf(bytesInputStream, file)) {
                Assert.assertEquals("Lorem ipsum dolor sit amen.\r\n", FileUtil.extractContent(null, file, inputStream, pdfInputStream));
            }
        }
    }
}
