package com.sismics.util.format;

import com.sismics.docs.core.util.format.PdfFormatHandler;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

public class TestPdfFormatHandler {
    @Test
    public void testIssue373() throws Exception {
        PdfFormatHandler formatHandler = new PdfFormatHandler();
        String content = formatHandler.extractContent("deu", Paths.get(ClassLoader.getSystemResource("file/issue373.pdf").toURI()));
        Assert.assertTrue(content.contains("Aufrechterhaltung"));
        Assert.assertTrue(content.contains("Außentemperatur"));
        Assert.assertTrue(content.contains("Grundumsatzmessungen"));
        Assert.assertTrue(content.contains("ermitteln"));
    }
}
