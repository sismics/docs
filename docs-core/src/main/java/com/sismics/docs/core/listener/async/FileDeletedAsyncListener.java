package com.sismics.docs.core.listener.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;

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

        // TODO Delete the file from storage
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.deleteDocument(fileDeletedAsyncEvent.getFile().getId());
    }
}
