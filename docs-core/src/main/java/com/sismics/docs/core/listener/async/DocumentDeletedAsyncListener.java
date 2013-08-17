package com.sismics.docs.core.listener.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;

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
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.deleteDocument(documentDeletedAsyncEvent.getDocument().getId());
    }
}
