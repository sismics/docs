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
import com.sismics.util.Scalr;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.mime.MimeTypeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
     * Optical character recognition on an image.
     *
     * @param language Language to OCR
     * @param image Buffered image
     * @return Content extracted
     */
    public static String ocrFile(String language, BufferedImage image) {
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

        // Save the file
        Cipher cipher = EncryptionUtil.getEncryptionCipher(user.getPrivateKey());
        Path path = DirectoryUtil.getStorageDirectory().resolve(file.getId());
        try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
            Files.copy(new CipherInputStream(inputStream, cipher), path);
        }

        // Update the user quota
        user.setStorageCurrent(user.getStorageCurrent() + fileSize);
        userDao.updateQuota(user);

        // Raise a new file created event and document updated event if we have a document
        startProcessingFile(fileId);
        FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
        fileCreatedAsyncEvent.setUserId(userId);
        fileCreatedAsyncEvent.setLanguage(language);
        fileCreatedAsyncEvent.setFile(file);
        fileCreatedAsyncEvent.setUnencryptedFile(unencryptedFile);
        ThreadLocalContext.get().addAsyncEvent(fileCreatedAsyncEvent);

        if (documentId != null) {
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
