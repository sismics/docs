package com.sismics.util.mime;

import com.google.common.base.Charsets;
import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility to check MIME types.
 *
 * @author bgamard
 */
public class MimeTypeUtil {
    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param file File to inspect
     * @param name File name
     * @return MIME type
     * @throws IOException e
     */
    public static String guessMimeType(Path file, String name) throws IOException {
        String mimeType = name == null ?
                null : URLConnection.getFileNameMap().getContentTypeFor(name);
        if (mimeType == null) {
            try (InputStream is = Files.newInputStream(file)) {
                final byte[] headerBytes = new byte[64];
                is.read(headerBytes);
                mimeType = guessMimeType(headerBytes, name);
            }
        }

        return guessOpenDocumentFormat(mimeType, file);
    }

    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param headerBytes File header (first bytes)
     * @param name File name
     * @return MIME type
     */
    public static String guessMimeType(byte[] headerBytes, String name) {
        String header = new String(headerBytes, StandardCharsets.US_ASCII);

        // Detect by header bytes
        if (header.startsWith("PK")) {
            return MimeType.APPLICATION_ZIP;
        } else if (header.startsWith("GIF87a") || header.startsWith("GIF89a")) {
            return MimeType.IMAGE_GIF;
        } else if (headerBytes[0] == ((byte) 0xff) && headerBytes[1] == ((byte) 0xd8)) {
            return MimeType.IMAGE_JPEG;
        } else if (headerBytes[0] == ((byte) 0x89) && headerBytes[1] == ((byte) 0x50) && headerBytes[2] == ((byte) 0x4e) && headerBytes[3] == ((byte) 0x47) &&
                headerBytes[4] == ((byte) 0x0d) && headerBytes[5] == ((byte) 0x0a) && headerBytes[6] == ((byte) 0x1a) && headerBytes[7] == ((byte) 0x0a)) {
            return MimeType.IMAGE_PNG;
        } else if (headerBytes[0] == ((byte) 0x25) && headerBytes[1] == ((byte) 0x50) && headerBytes[2] == ((byte) 0x44) && headerBytes[3] == ((byte) 0x46)) {
            return MimeType.APPLICATION_PDF;
        } else if (headerBytes[0] == ((byte) 0x00) && headerBytes[1] == ((byte) 0x00) && headerBytes[2] == ((byte) 0x00)
                && (headerBytes[3] == ((byte) 0x14) || headerBytes[3] == ((byte) 0x18) || headerBytes[3] == ((byte) 0x20))
                && headerBytes[4] == ((byte) 0x66) && headerBytes[5] == ((byte) 0x74) && headerBytes[6] == ((byte) 0x79) && headerBytes[7] == ((byte) 0x70)) {
            return MimeType.VIDEO_MP4;
        } else if (headerBytes[0] == ((byte) 0x1a) && headerBytes[1] == ((byte) 0x45) && headerBytes[2] == ((byte) 0xdf) && headerBytes[3] == ((byte) 0xa3)) {
            return MimeType.VIDEO_WEBM;
        }

        // Detect by file extension
        if (name != null) {
            if (name.endsWith(".txt")) {
                return MimeType.TEXT_PLAIN;
            } else if (name.endsWith(".csv")) {
                return MimeType.TEXT_CSV;
            }
        }

        return MimeType.DEFAULT;
    }
    
    /**
     * Get a file extension linked to a MIME type.
     * 
     * @param mimeType MIME type
     * @return File extension
     */
    public static String getFileExtension(String mimeType) {
        switch (mimeType) {
            case MimeType.APPLICATION_ZIP:
                return "zip";
            case MimeType.IMAGE_GIF:
                return "gif";
            case MimeType.IMAGE_JPEG:
                return "jpg";
            case MimeType.IMAGE_PNG:
                return "png";
            case MimeType.APPLICATION_PDF:
                return "pdf";
            case MimeType.OPEN_DOCUMENT_TEXT:
                return "odt";
            case MimeType.OFFICE_DOCUMENT:
                return "docx";
            case MimeType.TEXT_PLAIN:
                return "txt";
            case MimeType.TEXT_CSV:
                return "csv";
            case MimeType.VIDEO_MP4:
                return "mp4";
            case MimeType.VIDEO_WEBM:
                return "webm";
            default:
                return "bin";
        }
    }
    
    /**
     * Guess the MIME type of open document formats (docx and odt).
     * It's more costly than the simple header check, but needed because open document formats
     * are simple ZIP files on the outside and much bigger on the inside.
     * 
     * @param mimeType Currently detected MIME type
     * @param file File on disk
     * @return MIME type
     */
    private static String guessOpenDocumentFormat(String mimeType, Path file) {
        if (!MimeType.APPLICATION_ZIP.equals(mimeType)) {
            // open document formats are ZIP files
            return mimeType;
        }
        
        try (InputStream inputStream = Files.newInputStream(file);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream, Charsets.ISO_8859_1)) {
            ZipEntry archiveEntry = zipInputStream.getNextEntry();
            while (archiveEntry != null) {
                if (archiveEntry.getName().equals("mimetype")) {
                    // Maybe it's an ODT file
                    String content = new String(IOUtils.toByteArray(zipInputStream), Charsets.ISO_8859_1);
                    if (MimeType.OPEN_DOCUMENT_TEXT.equals(content.trim())) {
                        mimeType = MimeType.OPEN_DOCUMENT_TEXT;
                        break;
                    }
                } else if (archiveEntry.getName().equals("[Content_Types].xml")) {
                    // Maybe it's a DOCX file
                    String content = new String(IOUtils.toByteArray(zipInputStream), Charsets.ISO_8859_1);
                    if (content.contains(MimeType.OFFICE_DOCUMENT)) {
                        mimeType =  MimeType.OFFICE_DOCUMENT;
                        break;
                    } else if (content.contains(MimeType.OFFICE_PRESENTATION)) {
                        mimeType = MimeType.OFFICE_PRESENTATION;
                        break;
                    }
                }
    
                archiveEntry = zipInputStream.getNextEntry();
            }
        } catch (Exception e) {
            // In case of any error, just give up and keep the ZIP MIME type
            return mimeType;
        }
        
        return mimeType;
    }
}
