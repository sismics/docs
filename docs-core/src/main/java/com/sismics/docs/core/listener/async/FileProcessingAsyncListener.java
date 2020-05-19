package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.event.FileEvent;
import com.sismics.docs.core.event.FileUpdatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.format.FormatHandler;
import com.sismics.docs.core.util.format.FormatHandlerUtil;
import com.sismics.util.ImageUtil;
import com.sismics.util.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

/**
 * Listener on file processing.
 * 
 * @author bgamard
 */
public class FileProcessingAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileProcessingAsyncListener.class);

    /**
     * File created.
     *
     * @param event File created event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileCreatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + event.toString());
        }

        TransactionUtil.handle(() -> {
            // Generate thumbnail, extract content
            File file = new FileDao().getActiveById(event.getFileId());
            if (file == null) {
                // The file has been deleted since
                return;
            }
            processFile(event, file);

            // Update index with the file updated by side effect
            AppContext.getInstance().getIndexingHandler().createFile(file);
        });

        FileUtil.endProcessingFile(event.getFileId());
    }

    /**
     * File updated.
     *
     * @param event File updated event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileUpdatedAsyncEvent event) {
        log.info("File updated event: " + event.toString());

        TransactionUtil.handle(() -> {
            log.info("File processing phase 0: " + event.getFileId());

            // Generate thumbnail, extract content
            File file = new FileDao().getActiveById(event.getFileId());
            if (file == null) {
                // The file has been deleted since
                return;
            }
            processFile(event, file);

            // Update index with the file updated by side effect
            AppContext.getInstance().getIndexingHandler().updateFile(file);

            log.info("File processing phase 6: " + file.getId());
        });

        FileUtil.endProcessingFile(event.getFileId());
    }

    /**
     * Process the file (create/update).
     *
     * @param event File event
     * @param file Fresh file
     */
    private void processFile(FileEvent event, File file) {
        log.info("File processing phase 1: " + file.getId());

        // Find a format handler
        FormatHandler formatHandler = FormatHandlerUtil.find(file.getMimeType());
        if (formatHandler == null) {
            log.info("Format unhandled: " + file.getMimeType());
            return;
        }

        log.info("File processing phase 2: " + file.getId());

        // Get the creating user from the database for its private key
        UserDao userDao = new UserDao();
        User user = userDao.getById(file.getUserId());
        if (user == null) {
            // The user has been deleted meanwhile
            return;
        }

        log.info("File processing phase 3: " + file.getId());

        // Generate file variations
        try {
            Cipher cipher = EncryptionUtil.getEncryptionCipher(user.getPrivateKey());
            BufferedImage image = formatHandler.generateThumbnail(event.getUnencryptedFile());
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
        } catch (Exception e) {
            log.error("Unable to generate thumbnails for: " + file, e);
        }

        log.info("File processing phase 4: " + file.getId());

        // Extract text content from the file
        long startTime = System.currentTimeMillis();
        String content = null;
        try {
            content = formatHandler.extractContent(event.getLanguage(), event.getUnencryptedFile());
        } catch (Exception e) {
            log.error("Error extracting content from: " + file, e);
        }
        log.info(MessageFormat.format("File content extracted in {0}ms: " + file.getId(), System.currentTimeMillis() - startTime));

        // Save the file to database
        FileDao fileDao = new FileDao();
        if (fileDao.getActiveById(file.getId()) == null) {
            // The file has been deleted since the text extraction started, ignore the result
            return;
        }

        file.setContent(content);
        fileDao.updateContent(file);

        log.info("File processing phase 5: " + file.getId());
    }
}
