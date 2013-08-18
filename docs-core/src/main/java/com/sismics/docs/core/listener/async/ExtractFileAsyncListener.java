package com.sismics.docs.core.listener.async;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.event.ExtractFileAsyncEvent;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;

/**
 * Listener on extract content from all files.
 * 
 * @author bgamard
 */
public class ExtractFileAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ExtractFileAsyncListener.class);

    /**
     * Extract content from all files.
     * 
     * @param extractFileAsyncEvent Extract file content event
     * @throws Exception
     */
    @Subscribe
    public void on(final ExtractFileAsyncEvent extractFileAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Extract file content event: " + extractFileAsyncEvent.toString());
        }

        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                FileDao fileDao = new FileDao();
                DocumentDao documentDao = new DocumentDao();
                List<File> fileList = fileDao.findAll();
                for (File file : fileList) {
                    long startTime = System.currentTimeMillis();
                    Document document = documentDao.getById(file.getDocumentId());
                    file.setContent(FileUtil.extractContent(document, file));
                    TransactionUtil.commit();
                    log.info(MessageFormat.format("File content extracted in {0}ms", System.currentTimeMillis() - startTime));
                }
            }
        });
    }
}
