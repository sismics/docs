package com.sismics.docs.core.listener.async;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;

/**
 * Listener on file created.
 * 
 * @author bgamard
 */
public class FileCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileCreatedAsyncListener.class);

    /**
     * File created.
     * 
     * @param fileCreatedAsyncEvent File created event
     * @throws Exception
     */
    @Subscribe
    public void on(final FileCreatedAsyncEvent fileCreatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + fileCreatedAsyncEvent.toString());
        }

        // OCR the file
        final File file = fileCreatedAsyncEvent.getFile();
        long startTime = System.currentTimeMillis();
        final String content = FileUtil.extractContent(fileCreatedAsyncEvent.getDocument(), file, fileCreatedAsyncEvent.getInputStream());
        fileCreatedAsyncEvent.getInputStream().close();
        log.info(MessageFormat.format("File content extracted in {0}ms", System.currentTimeMillis() - startTime));
        
        // Store the OCR-ization result in the database
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                FileDao fileDao = new FileDao();
                file.setContent(content);
                fileDao.update(file);
            }
        });
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.createFile(fileCreatedAsyncEvent.getFile(), fileCreatedAsyncEvent.getDocument());
    }
}
