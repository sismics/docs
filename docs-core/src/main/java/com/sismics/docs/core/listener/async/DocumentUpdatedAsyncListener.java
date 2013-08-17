package com.sismics.docs.core.listener.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;

/**
 * Listener on document updated.
 * 
 * @author bgamard
 */
public class DocumentUpdatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DocumentUpdatedAsyncListener.class);

    /**
     * Document updated.
     * 
     * @param documentUpdatedAsyncEvent Document updated event
     * @throws Exception
     */
    @Subscribe
    public void on(final DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Document updated event: " + documentUpdatedAsyncEvent.toString());
        }

        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.updateDocument(documentUpdatedAsyncEvent.getDocument());
    }
}
