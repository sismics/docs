package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.FileUtil;
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
     * @param fileDeletedAsyncEvent File deleted event
     * @throws Exception e
     */
    @Subscribe
    public void on(final FileDeletedAsyncEvent fileDeletedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("File deleted event: " + fileDeletedAsyncEvent.toString());
        }

        // Delete the file from storage
        File file = fileDeletedAsyncEvent.getFile();
        FileUtil.delete(file);
        
        // Update index
        AppContext.getInstance().getIndexingHandler().deleteDocument(file.getId());
    }
}
