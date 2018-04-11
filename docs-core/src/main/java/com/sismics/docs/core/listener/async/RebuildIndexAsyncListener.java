package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.event.RebuildIndexAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Listener on rebuild index.
 * 
 * @author bgamard
 */
public class RebuildIndexAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RebuildIndexAsyncListener.class);

    /**
     * Rebuild Lucene index.
     * 
     * @param event Index rebuild event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void on(final RebuildIndexAsyncEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Rebuild index event: " + event.toString());
        }
        
        // Fetch all documents and files
        TransactionUtil.handle(() -> {
            // Fetch all documents
            DocumentDao documentDao = new DocumentDao();
            List<Document> documentList = documentDao.findAll();

            // Fetch all files
            FileDao fileDao = new FileDao();
            List<File> fileList = fileDao.findAll();

            // Rebuild index
            AppContext.getInstance().getIndexingHandler().rebuildIndex(documentList, fileList);
        });
    }
}
