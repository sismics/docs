package com.sismics.util.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.google.common.base.Charsets;
import com.sismics.docs.core.model.jpa.File;

/**
 * Utility to check MIME types.
 *
 * @author jtremeaux
 */
public class MimeTypeUtil {
    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param is Stream to inspect
     * @param name File name
     * @return MIME type
     * @throws IOException e
     */
    public static String guessMimeType(InputStream is, String name) throws IOException {
        byte[] headerBytes = new byte[64];
        is.mark(headerBytes.length);
        is.read(headerBytes);
        is.reset();
        return guessMimeType(headerBytes, name);
    }

    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param headerBytes File header (first bytes)
     * @param name File name
     * @return MIME type
     * @throws UnsupportedEncodingException e
     */
    public static String guessMimeType(byte[] headerBytes, String name) throws UnsupportedEncodingException {
        String header = new String(headerBytes, "US-ASCII");

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
            default:
                return "bin";
        }
    }
    
    /**
     * Guess the MIME type of open document formats (docx and odt).
     * It's more costly than the simple header check, but needed because open document formats
     * are simple ZIP files on the outside and much bigger on the inside.
     * 
     * @param file File 
     * @param inputStream Input stream
     * @return MIME type
     */
    public static String guessOpenDocumentFormat(File file, InputStream inputStream) {
        if (!MimeType.APPLICATION_ZIP.equals(file.getMimeType())) {
            // open document formats are ZIP files
            return file.getMimeType();
        }
        
        String mimeType = file.getMimeType();
        try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream, Charsets.ISO_8859_1.name())) {
            ArchiveEntry archiveEntry = archiveInputStream.getNextEntry();
            while (archiveEntry != null) {
                if (archiveEntry.getName().equals("mimetype")) {
                    // Maybe it's an ODT file
                    String content = new String(IOUtils.toByteArray(archiveInputStream), Charsets.ISO_8859_1);
                    if (MimeType.OPEN_DOCUMENT_TEXT.equals(content.trim())) {
                        mimeType = MimeType.OPEN_DOCUMENT_TEXT;
                        break;
                    }
                } else if (archiveEntry.getName().equals("[Content_Types].xml")) {
                    // Maybe it's a DOCX file
                    String content = new String(IOUtils.toByteArray(archiveInputStream), Charsets.ISO_8859_1);
                    if (content.contains(MimeType.OFFICE_DOCUMENT)) {
                        mimeType =  MimeType.OFFICE_DOCUMENT;
                        break;
                    }
                }
    
                archiveEntry = archiveInputStream.getNextEntry();
            }
            
            inputStream.reset();
        } catch (Exception e) {
            // In case of any error, just give up and keep the ZIP MIME type
            return file.getMimeType();
        }
        
        return mimeType;
    }
}
