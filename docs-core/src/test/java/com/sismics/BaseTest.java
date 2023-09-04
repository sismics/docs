package com.sismics;

import java.io.InputStream;
import java.net.URL;

public abstract class BaseTest {

    public static final String FILE_CSV = "document.csv";
    
    public static final String FILE_DOCX = "document.docx";

    public static final String FILE_GIF = "image.gif";

    public static final String FILE_JPG = "apollo_portrait.jpg";

    public static final String FILE_JPG2 = "apollo_landscape.jpg";

    public static final String FILE_MP4 = "video.mp4";

    public static final String FILE_ODT = "document.odt";

    public static final String FILE_PDF = "udhr.pdf";

    public static final String FILE_PDF_ENCRYPTED = "udhr_encrypted.pdf";

    public static final String FILE_PDF_SCANNED = "scanned.pdf";

    public static final String FILE_PNG = "image.png";

    public static final String FILE_PPTX = "apache.pptx";

    public static final String FILE_TXT = "document.txt";

    public static final String FILE_WEBM = "video.webm";

    public static final String FILE_XLSX = "document.xlsx";

    public static final String FILE_ZIP = "document.zip";

    public static URL getResource(String fileName) {
        return ClassLoader.getSystemResource("file/" + fileName);
    }

    public static InputStream getSystemResourceAsStream(String fileName) {
        return ClassLoader.getSystemResourceAsStream("file/" + fileName);
    }
}
