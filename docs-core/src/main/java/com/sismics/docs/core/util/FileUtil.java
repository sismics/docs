package com.sismics.docs.core.util;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.tess4j.Tesseract;
import com.sismics.util.ImageDeskew;
import com.sismics.util.ImageUtil;
import com.sismics.util.Scalr;
import com.sismics.util.VideoUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.mime.MimeTypeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * File entity utilities.
 * 
 * @author bgamard
 */
public class FileUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * File ID of files currently being processed.
     */
    private static Set<String> processingFileSet = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Extract content from a file.
     * 
     * @param language Language to extract
     * @param file File to extract
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @return Content extract
     */
    public static String extractContent(String language, File file, Path unencryptedFile, Path unencryptedPdfFile) {
        String content = null;
        
        if (ImageUtil.isImage(file.getMimeType())) {
            content = ocrFile(unencryptedFile, language);
        } else if (VideoUtil.isVideo(file.getMimeType())) {
            content = VideoUtil.getMetadata(unencryptedFile);
        } else if (unencryptedPdfFile != null) {
            content = PdfUtil.extractPdf(unencryptedPdfFile, language);
        }
        
        return content;
    }

    /**
     * Optical character recognition on an image.
     *
     * @param image Buffered image
     * @param language Language to OCR
     * @return Content extracted
     */
    public static String ocrFile(BufferedImage image, String language) {
        // Upscale, grayscale and deskew the image
        String content = null;
        BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, 3500, Scalr.OP_ANTIALIAS, Scalr.OP_GRAYSCALE);
        image.flush();
        ImageDeskew imageDeskew = new ImageDeskew(resizedImage);
        BufferedImage deskewedImage = Scalr.rotate(resizedImage, - imageDeskew.getSkewAngle(), Scalr.OP_ANTIALIAS, Scalr.OP_GRAYSCALE);
        resizedImage.flush();
        image = deskewedImage;

        // OCR the file
        try {
            Tesseract instance = Tesseract.getInstance();
            log.info("Starting OCR with TESSDATA_PREFIX=" + System.getenv("TESSDATA_PREFIX") + ";LC_NUMERIC=" + System.getenv("LC_NUMERIC"));
            instance.setLanguage(language);
            content = instance.doOCR(image);
        } catch (Throwable e) {
            log.error("Error while OCR-izing the image", e);
        }

        return content;
    }

    /**
     * Optical character recognition on a file.
     *
     * @param unecryptedFile Unencrypted file
     * @param language Language to OCR
     * @return Content extracted
     */
    private static String ocrFile(Path unecryptedFile, String language) {
        BufferedImage image;
        try (InputStream inputStream = Files.newInputStream(unecryptedFile)) {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("Error reading the image", e);
            return null;
        }

        return ocrFile(image, language);
    }
    
    /**
     * Save a file on the storage filesystem.
     * 
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @param file File to save
     * @param privateKey Private key used for encryption
     */
    public static void save(Path unencryptedFile, Path unencryptedPdfFile, File file, String privateKey) throws Exception {
        Cipher cipher = EncryptionUtil.getEncryptionCipher(privateKey);
        Path path = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
            Files.copy(new CipherInputStream(inputStream, cipher), path);
        }

        // Generate file variations (errors non-blocking)
        try {
            saveVariations(file, unencryptedFile, unencryptedPdfFile, cipher);
        } catch (Exception e) {
            log.error("Unable to generate thumbnails", e);
        }
    }

    /**
     * Generate file variations.
     * 
     * @param file File from database
     * @param unencryptedFile Unencrypted file
     * @param unencryptedPdfFile Unencrypted PDF file
     * @param cipher Cipher to use for encryption
     */
    private static void saveVariations(File file, Path unencryptedFile, Path unencryptedPdfFile, Cipher cipher) throws Exception {
        BufferedImage image = null;
        if (ImageUtil.isImage(file.getMimeType())) {
            try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
                image = ImageIO.read(inputStream);
            }
        } else if (VideoUtil.isVideo(file.getMimeType())) {
            image = VideoUtil.getThumbnail(unencryptedFile);
        } else if (unencryptedPdfFile != null) {
            // Generate preview from the first page of the PDF
            image = PdfUtil.renderFirstPage(unencryptedPdfFile);
        }
        
        if (image != null) {
            // Generate thumbnails from image
            BufferedImage web = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, 1280);
            BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, 256);
            image.flush();
            
            // Write "web" encrypted image
            Path outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_web");
            try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                ImageUtil.writeJpeg(web, outputStream);
            }
            
            // Write "thumb" encrypted image
            outputFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_thumb");
            try (OutputStream outputStream = new CipherOutputStream(Files.newOutputStream(outputFile), cipher)) {
                ImageUtil.writeJpeg(thumbnail, outputStream);
            }
        }
    }

    /**
     * Remove a file from the storage filesystem.
     * 
     * @param file File to delete
     */
    public static void delete(File file) throws IOException {
        Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        Path webFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_web");
        Path thumbnailFile = DirectoryUtil.getStorageDirectory().resolve(file.getId() + "_thumb");
        
        if (Files.exists(storedFile)) {
            Files.delete(storedFile);
        }
        if (Files.exists(webFile)) {
            Files.delete(webFile);
        }
        if (Files.exists(thumbnailFile)) {
            Files.delete(thumbnailFile);
        }
    }

    /**
     * Create a new file.
     *
     * @param name File name, can be null
     * @param unencryptedFile Path to the unencrypted file
     * @param fileSize File size
     * @param language File language, can be null if associated to no document
     * @param userId User ID creating the file
     * @param documentId Associated document ID or null if no document
     * @return File ID
     * @throws Exception e
     */
    public static String createFile(String name, Path unencryptedFile, long fileSize, String language, String userId, String documentId) throws Exception {
        // Validate mime type
        String mimeType;
        try {
            mimeType = MimeTypeUtil.guessMimeType(unencryptedFile, name);
        } catch (IOException e) {
            throw new IOException("ErrorGuessMime", e);
        }

        // Validate user quota
        UserDao userDao = new UserDao();
        User user = userDao.getById(userId);
        if (user.getStorageCurrent() + fileSize > user.getStorageQuota()) {
            throw new IOException("QuotaReached");
        }

        // Validate global quota
        String globalStorageQuotaStr = System.getenv(Constants.GLOBAL_QUOTA_ENV);
        if (!Strings.isNullOrEmpty(globalStorageQuotaStr)) {
            long globalStorageQuota = Long.valueOf(globalStorageQuotaStr);
            long globalStorageCurrent = userDao.getGlobalStorageCurrent();
            if (globalStorageCurrent + fileSize > globalStorageQuota) {
                throw new IOException("QuotaReached");
            }
        }

        // Get files of this document
        FileDao fileDao = new FileDao();
        int order = 0;
        if (documentId != null) {
            for (File file : fileDao.getByDocumentId(userId, documentId)) {
                file.setOrder(order++);
            }
        }

        // Create the file
        File file = new File();
        file.setOrder(order);
        file.setDocumentId(documentId);
        file.setName(StringUtils.abbreviate(name, 200));
        file.setMimeType(mimeType);
        file.setUserId(userId);
        String fileId = fileDao.create(file, userId);

        // Guess the mime type a second time, for open document format (first detected as simple ZIP file)
        file.setMimeType(MimeTypeUtil.guessOpenDocumentFormat(file, unencryptedFile));

        // Convert to PDF if necessary (for thumbnail and text extraction)
        java.nio.file.Path unencryptedPdfFile = PdfUtil.convertToPdf(file, unencryptedFile);

        // Save the file
        FileUtil.save(unencryptedFile, unencryptedPdfFile, file, user.getPrivateKey());

        // Update the user quota
        user.setStorageCurrent(user.getStorageCurrent() + fileSize);
        userDao.updateQuota(user);

        // Raise a new file created event and document updated event if we have a document
        if (documentId != null) {
            startProcessingFile(fileId);
            FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
            fileCreatedAsyncEvent.setUserId(userId);
            fileCreatedAsyncEvent.setLanguage(language);
            fileCreatedAsyncEvent.setFile(file);
            fileCreatedAsyncEvent.setUnencryptedFile(unencryptedFile);
            fileCreatedAsyncEvent.setUnencryptedPdfFile(unencryptedPdfFile);
            ThreadLocalContext.get().addAsyncEvent(fileCreatedAsyncEvent);

            DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
            documentUpdatedAsyncEvent.setUserId(userId);
            documentUpdatedAsyncEvent.setDocumentId(documentId);
            ThreadLocalContext.get().addAsyncEvent(documentUpdatedAsyncEvent);
        }

        return fileId;
    }

    /**
     * Start processing a file.
     *
     * @param fileId File ID
     */
    public static void startProcessingFile(String fileId) {
        processingFileSet.add(fileId);
    }

    /**
     * End processing a file.
     *
     * @param fileId File ID
     */
    public static void endProcessingFile(String fileId) {
        processingFileSet.remove(fileId);
    }

    /**
     * Return true if a file is currently processing.
     *
     * @param fileId File ID
     * @return True if the file is processing
     */
    public static boolean isProcessingFile(String fileId) {
        return processingFileSet.contains(fileId);
    }
}
