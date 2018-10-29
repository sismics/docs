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

        // Clear the index
        AppContext.getInstance().getIndexingHandler().clearIndex();

        // Index all documents
        TransactionUtil.handle(() -> {
            int offset = 0;
            DocumentDao documentDao = new DocumentDao();
            List<Document> documentList;
            do {
                documentList = documentDao.findAll(offset, 100);
                AppContext.getInstance().getIndexingHandler().createDocuments(documentList);
                offset += 100;
            } while (documentList.size() > 0);
        });

        // Index all files
        TransactionUtil.handle(() -> {
            int offset = 0;
            FileDao fileDao = new FileDao();
            List<File> fileList;
            do {
                fileList = fileDao.findAll(offset, 100);
                AppContext.getInstance().getIndexingHandler().createFiles(fileList);
                offset += 100;
            } while (fileList.size() > 0);
        });

        if (log.isInfoEnabled()) {
            log.info("Rebuilding index done");
        }
    }
}
