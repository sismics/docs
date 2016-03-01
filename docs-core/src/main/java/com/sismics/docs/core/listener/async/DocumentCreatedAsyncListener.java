package com.sismics.docs.core.listener.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.ContributorDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.docs.core.util.TransactionUtil;

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
     * @param event Document created event
     * @throws Exception
     */
    @Subscribe
    public void on(final DocumentCreatedAsyncEvent event) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Document created event: " + event.toString());
        }

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // Add the first contributor (the creator of the document)
                ContributorDao contributorDao = new ContributorDao();
                Contributor contributor = new Contributor();
                contributor.setDocumentId(event.getDocument().getId());
                contributor.setUserId(event.getUserId());
                contributorDao.create(contributor);
            }
        });
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.createDocument(event.getDocument());
    }
}
