package com.sismics.docs.core.listener.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;

/**
 * Listener on document created.
 * 
 * @author bgamard
 */
public class DocumentCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentCreatedAsyncListener.class);

    /**
     * Document created.
     * 
     * @param documentCreatedAsyncEvent Document created event
     * @throws Exception
     */
    @Subscribe
    public void on(final DocumentCreatedAsyncEvent documentCreatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Document created event: " + documentCreatedAsyncEvent.toString());
        }

        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.createDocument(documentCreatedAsyncEvent.getDocument());
    }
}
