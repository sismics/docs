package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Delete the file from storage
        FileUtil.delete(event.getFileId());

        TransactionUtil.handle(() -> {
            // Update index
            AppContext.getInstance().getIndexingHandler().deleteDocument(event.getFileId());
        });
    }
}
