package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.ContributorDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
     * @param event Document updated event
     */
    @Subscribe
    public void on(final DocumentUpdatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Document updated event: " + event.toString());
        }

        TransactionUtil.handle(() -> {
            // Update Lucene index
            DocumentDao documentDao = new DocumentDao();
            LuceneDao luceneDao = new LuceneDao();
            Document document = documentDao.getById(event.getDocumentId());
            if (document == null) {
                // Document deleted since event fired
                return;
            }
            luceneDao.updateDocument(document);

            // Update contributors list
            ContributorDao contributorDao = new ContributorDao();
            List<Contributor> contributorList = contributorDao.findByDocumentId(event.getDocumentId());

            // Check if the user firing this event is not already a contributor
            for (Contributor contributor : contributorList) {
                if (contributor.getUserId().equals(event.getUserId())) {
                    // The current user is already a contributor on this document, don't do anything
                    return;
                }
            }

            // Add a new contributor
            Contributor contributor = new Contributor();
            contributor.setDocumentId(event.getDocumentId());
            contributor.setUserId(event.getUserId());
            contributorDao.create(contributor);
        });
    }
}
