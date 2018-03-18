package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Listener on file created.
 * 
 * @author bgamard
 */
public class FileCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileCreatedAsyncListener.class);

    /**
     * File created.
     * 
     * @param event File created event
     */
    @Subscribe
    public void on(final FileCreatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + event.toString());
        }

        // Find a format handler
        final File file = event.getFile();
        FormatHandler formatHandler = FormatHandlerUtil.find(file.getMimeType());
        if (formatHandler == null) {
            log.error("Format unhandled: " + file.getMimeType());
            return;
        }

        // Get the user from the database
        final AtomicReference<User> user = new AtomicReference<>();
        TransactionUtil.handle(() -> {
            UserDao userDao = new UserDao();
            user.set(userDao.getById(event.getUserId()));
        });
        if (user.get() == null) {
            // The user has been deleted meanwhile
            return;
        }

        // Generate file variations
        try {
            Cipher cipher = EncryptionUtil.getEncryptionCipher(user.get().getPrivateKey());
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
            log.error("Unable to generate thumbnails", e);
        }

        // Extract text content from the file
        long startTime = System.currentTimeMillis();
        final AtomicReference<String> content = new AtomicReference<>();
        try {
            content.set(formatHandler.extractContent(event.getLanguage(), event.getUnencryptedFile()));
        } catch (Exception e) {
            log.error("Error extracting content from: " + event.getFile());
        }
        log.info(MessageFormat.format("File content extracted in {0}ms", System.currentTimeMillis() - startTime));

        // Save the file to database
        TransactionUtil.handle(() -> {
            FileDao fileDao = new FileDao();
            if (fileDao.getActiveById(file.getId()) == null) {
                // The file has been deleted since the text extraction started, ignore the result
                return;
            }

            file.setContent(content.get());
            fileDao.update(file);
        });

        if (file.getDocumentId() != null) {
            // Update Lucene index
            LuceneDao luceneDao = new LuceneDao();
            luceneDao.createFile(event.getFile());
        }

        FileUtil.endProcessingFile(file.getId());
    }
}
