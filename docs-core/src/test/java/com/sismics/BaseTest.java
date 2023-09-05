package com.sismics;

import java.io.InputStream;
import java.net.URL;

public abstract class BaseTest {

    protected static final String FILE_CSV = "document.csv";

    protected static final String FILE_DOCX = "document.docx";

    protected static final String FILE_GIF = "image.gif";

    protected static final String FILE_JPG = "apollo_portrait.jpg";

    protected static final Long FILE_JPG_SIZE = 7_907L;

    protected static final String FILE_JPG2 = "apollo_landscape.jpg";

    protected static final String FILE_MP4 = "video.mp4";

    protected static final String FILE_ODT = "document.odt";

    protected static final String FILE_PDF = "udhr.pdf";

    protected static final String FILE_PDF_ENCRYPTED = "udhr_encrypted.pdf";

    protected static final String FILE_PDF_SCANNED = "scanned.pdf";

    protected static final String FILE_PNG = "image.png";

    protected static final String FILE_PPTX = "apache.pptx";

    protected static final String FILE_TXT = "document.txt";

    protected static final String FILE_WEBM = "video.webm";

    protected static final String FILE_XLSX = "document.xlsx";

    protected static final String FILE_ZIP = "document.zip";

    protected static URL getResource(String fileName) {
        return ClassLoader.getSystemResource("file/" + fileName);
    }

    protected static InputStream getSystemResourceAsStream(String fileName) {
        return ClassLoader.getSystemResourceAsStream("file/" + fileName);
    }
}
