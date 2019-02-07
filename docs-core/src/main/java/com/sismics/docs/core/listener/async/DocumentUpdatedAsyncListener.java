package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.ContributorDao;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
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
    @AllowConcurrentEvents
    public void on(final DocumentUpdatedAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Document updated event: " + event.toString());
        }

        TransactionUtil.handle(() -> {
            // Get the document
            DocumentDao documentDao = new DocumentDao();
            Document document = documentDao.getById(event.getDocumentId());
            if (document == null) {
                // Document deleted since event fired
                return;
            }

            // Set the main file
            FileDao fileDao = new FileDao();
            List<File> fileList = fileDao.getByDocumentId(null, event.getDocumentId());
            if (fileList.isEmpty()) {
                document.setFileId(null);
            } else {
                document.setFileId(fileList.get(0).getId());
            }

            // Update database and index
            documentDao.updateFileId(document);
            AppContext.getInstance().getIndexingHandler().updateDocument(document);

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
