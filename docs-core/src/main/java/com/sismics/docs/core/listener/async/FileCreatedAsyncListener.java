package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.lucene.LuceneDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

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
     */
    @Subscribe
    public void on(final FileCreatedAsyncEvent fileCreatedAsyncEvent) {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + fileCreatedAsyncEvent.toString());
        }

        // Extract text content from the file
        final File file = fileCreatedAsyncEvent.getFile();
        long startTime = System.currentTimeMillis();
        final String content = FileUtil.extractContent(fileCreatedAsyncEvent.getLanguage(), file,
                fileCreatedAsyncEvent.getUnencryptedFile(), fileCreatedAsyncEvent.getUnencryptedPdfFile());
        log.info(MessageFormat.format("File content extracted in {0}ms", System.currentTimeMillis() - startTime));
        
        // Store the text content in the database
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                FileDao fileDao = new FileDao();
                if (fileDao.getActiveById(file.getId()) == null) {
                    // The file has been deleted since the text extraction started, ignore the result
                    return;
                }
                
                file.setContent(content);
                fileDao.update(file);
            }
        });
        
        // Update Lucene index
        LuceneDao luceneDao = new LuceneDao();
        luceneDao.createFile(fileCreatedAsyncEvent.getFile());
    }
}
