package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener on document deleted.
 * 
 * @author bgamard
 */
public class DocumentDeletedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentDeletedAsyncListener.class);

    /**
     * Document deleted.
     * 
     * @param documentDeletedAsyncEvent Document deleted event
     * @throws Exception
     */
    @Subscribe
    public void on(final DocumentDeletedAsyncEvent documentDeletedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Document deleted event: " + documentDeletedAsyncEvent.toString());
        }

        // Update Lucene index
        AppContext.getInstance().getIndexingHandler().deleteDocument(documentDeletedAsyncEvent.getDocumentId());
    }
}
