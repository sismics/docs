package com.sismics.docs.core.listener.async;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;

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
     * @param fileDeletedAsyncEvent File deleted event
     * @throws Exception
     */
    @Subscribe
    public void on(final FileDeletedAsyncEvent fileDeletedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("File deleted event: " + fileDeletedAsyncEvent.toString());
        }

        // Delete the file from storage
        File file = fileDeletedAsyncEvent.getFile();
        java.io.File storedFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
        java.io.File webFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_web").toFile();
        java.io.File thumbnailFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId() + "_thumb").toFile();
        
        if (storedFile.exists()) {
            storedFile.delete();
        }
        if (webFile.exists()) {
            webFile.delete();
        }
        if (thumbnailFile.exists()) {
            thumbnailFile.delete();
        }
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.deleteDocument(file.getId());
    }
}
