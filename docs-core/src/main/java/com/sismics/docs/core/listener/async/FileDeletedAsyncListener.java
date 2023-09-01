package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Listener on file deleted.
 *
 * @author bgamard
 */
public class FileDeletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileDeletedAsyncListener.class);

    /**
     * File deleted.
     *
     * @param event File deleted event
     * @throws Exception e
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final FileDeletedAsyncEvent event) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("File deleted event: " + event.toString());
        }
        TransactionUtil.handle(() -> {
            // Update the user quota
            UserDao userDao = new UserDao();
            User user = userDao.getById(event.getUserId());
            if (user != null) {
                Long fileSize = event.getFileSize();

                if (fileSize == -1) {
                    // The file size was not in the database, in this case we need to get from the unencrypted size.
                    Path storedFile = DirectoryUtil.getStorageDirectory().resolve(event.getFileId());
                    try (InputStream fileInputStream = Files.newInputStream(storedFile);
                         InputStream inputStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey());
                         CountingInputStream countingInputStream = new CountingInputStream(inputStream);
                    ) {
                        IOUtils.copy(countingInputStream, NullOutputStream.NULL_OUTPUT_STREAM);
                    } catch (Exception e) {
                        // Do nothing in this case
                    }
                }

                if (fileSize != -1) {
                    user.setStorageCurrent(user.getStorageCurrent() - fileSize);
                    userDao.updateQuota(user);
                }
            }
        });

        // Delete the file from storage
        FileUtil.delete(event.getFileId());

        TransactionUtil.handle(() -> {
            // Update index
            AppContext.getInstance().getIndexingHandler().deleteDocument(event.getFileId());
        });
    }
}
