package com.sismics.docs.core.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.CheckIndex.Status;
import org.apache.lucene.index.CheckIndex.Status.SegmentInfoStatus;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.event.RebuildIndexAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.TransactionUtil;

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
     * Directory reader.
     */
    private DirectoryReader directoryReader;
    
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
            Path luceneDirectory = DirectoryUtil.getLuceneDirectory();
            log.info("Using file Lucene storage: {}", luceneDirectory);
            try {
                directory = new SimpleFSDirectory(luceneDirectory, new SingleInstanceLockFactory());
            } catch (IOException e) {
                log.error("Error initializing Lucene index", e);
            }
        }
        
        // Check index version and rebuild it if necessary
        log.info("Checking index health and version");
        try (CheckIndex checkIndex = new CheckIndex(directory)) {
            Status status = checkIndex.checkIndex();
            if (status.clean) {
                for (SegmentInfoStatus segmentInfo : status.segmentInfos) {
                    if (!segmentInfo.version.onOrAfter(Version.LATEST)) {
                        RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
                        AppContext.getInstance().getAsyncEventBus().post(rebuildIndexAsyncEvent);
                        break;
                    }
                }
            } else {
                RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
                AppContext.getInstance().getAsyncEventBus().post(rebuildIndexAsyncEvent);
            }
        } catch (IOException e) {
            log.error("Error checking index", e);
        }
    }

    @Override
    protected void shutDown() {
        if (directoryReader != null) {
            try {
                directoryReader.close();
            } catch (IOException e) {
                log.error("Error closing the index reader", e);
            }
        }
        if (directory != null) {
            try {
                directory.close();
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
     * Destroy and rebuild Lucene index.
     * 
     * @throws Exception 
     */
    public void rebuildIndex() throws Exception {
        RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
        AppContext.getInstance().getAsyncEventBus().post(rebuildIndexAsyncEvent);
    }
    
    /**
     * Getter of directory.
     *
     * @return the directory
     */
    public Directory getDirectory() {
        return directory;
    }

    /**
     * Returns a valid directory reader.
     * Take care of reopening the reader if the index has changed
     * and closing the previous one.
     *
     * @return the directoryReader
     */
    public DirectoryReader getDirectoryReader() {
        if (directoryReader == null) {
            try {
                if (!DirectoryReader.indexExists(directory)) {
                    return null;
                }
                directoryReader = DirectoryReader.open(directory);
            } catch (IOException e) {
                log.error("Error creating the directory reader", e);
            }
        } else {
            try {
                DirectoryReader newReader = DirectoryReader.openIfChanged(directoryReader);
                if (newReader != null) {
                    directoryReader.close();
                    directoryReader = newReader;
                }
            } catch (IOException e) {
                log.error("Error while reopening the directory reader", e);
            }
        }
        return directoryReader;
    }
}
