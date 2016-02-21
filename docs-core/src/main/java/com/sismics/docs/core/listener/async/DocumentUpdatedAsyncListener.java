package com.sismics.docs.core.listener.async;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.ContributorDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.docs.core.util.TransactionUtil;

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
     * @throws Exception
     */
    @Subscribe
    public void on(final DocumentUpdatedAsyncEvent event) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Document updated event: " + event.toString());
        }

        // Update contributors list
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                ContributorDao contributorDao = new ContributorDao();
                List<Contributor> contributorList = contributorDao.findByDocumentId(event.getDocument().getId());
                
                // Check if the user firing this event is not already a contributor
                for (Contributor contributor : contributorList) {
                    if (contributor.getUserId().equals(event.getUserId())) {
                        // The current user is already a contributor on this document, don't do anything
                        return;
                    }
                }
                
                // Add a new contributor
                Contributor contributor = new Contributor();
                contributor.setDocumentId(event.getDocument().getId());
                contributor.setUserId(event.getUserId());
                contributorDao.create(contributor);
            }
        });
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.updateDocument(event.getDocument());
    }
}
