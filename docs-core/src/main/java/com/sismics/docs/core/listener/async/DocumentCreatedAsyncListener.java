package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.ContributorDao;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final DocumentCreatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Document created event: " + event.toString());
        }

        TransactionUtil.handle(() -> {
            // Fetch a fresh document
            Document document = new DocumentDao().getById(event.getDocumentId());
            if (document == null) {
                // The document has been deleted since
                return;
            }

            // Add the first contributor (the creator of the document)
            ContributorDao contributorDao = new ContributorDao();
            Contributor contributor = new Contributor();
            contributor.setDocumentId(event.getDocumentId());
            contributor.setUserId(event.getUserId());
            contributorDao.create(contributor);

            // Update index
            AppContext.getInstance().getIndexingHandler().createDocument(document);
        });
    }
}
