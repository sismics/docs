package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.TransactionUtil;
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
     * @param event Document deleted event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentDeletedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Document deleted event: " + event.toString());
        }

        TransactionUtil.handle(() -> {
            // Update index
            AppContext.getInstance().getIndexingHandler().deleteDocument(event.getDocumentId());
        });
    }
}
