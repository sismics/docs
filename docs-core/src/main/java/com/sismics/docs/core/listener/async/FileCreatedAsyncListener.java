package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.PdfUtil;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.util.mime.MimeTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
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

        // Guess the mime type a second time, for open document format (first detected as simple ZIP file)
        final File file = event.getFile();
        file.setMimeType(MimeTypeUtil.guessOpenDocumentFormat(file, event.getUnencryptedFile()));

        // Convert to PDF if necessary (for thumbnail and text extraction)
        Path unencryptedPdfFile = null;
        try {
            unencryptedPdfFile = PdfUtil.convertToPdf(file, event.getUnencryptedFile());
        } catch (Exception e) {
            log.error("Unable to convert to PDF", e);
        }

        // Get the user from the database
        final AtomicReference<User> user = new AtomicReference<>();
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                UserDao userDao = new UserDao();
                user.set(userDao.getById(event.getUserId()));
            }
        });
        if (user.get() == null) {
            // The user has been deleted meanwhile
            return;
        }

        // Generate file variations
        try {
            Cipher cipher = EncryptionUtil.getEncryptionCipher(user.get().getPrivateKey());
            FileUtil.saveVariations(file, event.getUnencryptedFile(), unencryptedPdfFile, cipher);
        } catch (Exception e) {
            log.error("Unable to generate thumbnails", e);
        }

        // Extract text content from the file
        long startTime = System.currentTimeMillis();
        final String content = FileUtil.extractContent(event.getLanguage(), file,
                event.getUnencryptedFile(), unencryptedPdfFile);
        log.info(MessageFormat.format("File content extracted in {0}ms", System.currentTimeMillis() - startTime));

        // Save the file to database
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                FileDao fileDao = new FileDao();
                if (fileDao.getActiveById(file.getId()) == null) {
                    // The file has been deleted since the text extraction started, ignore the result
                    return;
                }
                
                file.setContent(content);
                fileDao.update(file);
            }
        });
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.createFile(event.getFile());

        FileUtil.endProcessingFile(file.getId());
    }
}
