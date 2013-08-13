package com.sismics.docs.core.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.TransactionUtil;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Indexing service.
 *
 * @author bgamard
 */
public class IndexingService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    /**
     * Lucene directory.
     */
    private Directory directory;
    
    /**
     * Lucene storage config.
     */
    private String luceneStorageConfig;
    
    public IndexingService(String luceneStorageConfig) {
        this.luceneStorageConfig = luceneStorageConfig;
    }

    @Override
    protected void startUp() {
        // RAM directory storage by default
        if (luceneStorageConfig == null || luceneStorageConfig.equals(Constants.LUCENE_DIRECTORY_STORAGE_RAM)) {
            directory = new RAMDirectory();
            log.info("Using RAM Lucene storage");
        } else if (luceneStorageConfig.equals(Constants.LUCENE_DIRECTORY_STORAGE_FILE)) {
            File luceneDirectory = DirectoryUtil.getLuceneDirectory();
            log.info("Using file Lucene storage: {}", luceneDirectory);
            try {
                directory = new SimpleFSDirectory(luceneDirectory, new SimpleFSLockFactory());
            } catch (IOException e) {
                log.error("Error initializing Lucene index", e);
            }
        }
    }

    @Override
    protected void shutDown() {
        Directory luceneIndex = AppContext.getInstance().getLuceneDirectory();
        if (luceneIndex != null) {
            try {
                luceneIndex.close();
            } catch (IOException e) {
                log.error("Error closing Lucene index", e);
            }
        }
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        TransactionUtil.handle(new Runnable() {
            @Override
            public void run() {
                // NOP
            }
        });
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.HOURS);
    }
    
    /**
     * Getter of directory.
     *
     * @return the directory
     */
    public Directory getDirectory() {
        return directory;
    }
}
