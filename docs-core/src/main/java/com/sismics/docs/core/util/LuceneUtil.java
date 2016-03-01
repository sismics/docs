package com.sismics.docs.core.util;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.model.context.AppContext;

/**
 * Lucene utils.
 * 
 * @author bgamard
 */
public class LuceneUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LuceneUtil.class);

    /**
     * Encapsulate a process into a Lucene context.
     * 
     * @param runnable
     * @throws IOException 
     */
    public static void handle(LuceneRunnable runnable) {
        // Standard analyzer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        
        // Automatically commit when closing this writer
        config.setCommitOnClose(true);
        
        // Merge sequentially, because Lucene writing is already done asynchronously 
        config.setMergeScheduler(new SerialMergeScheduler());
        
        // Creating index writer
        Directory directory = AppContext.getInstance().getIndexingService().getDirectory();
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, config);
        } catch (IOException e) {
            log.error("Cannot create IndexWriter", e);
        }

        try {
            runnable.run(indexWriter);
        } catch (Exception e) {
            log.error("Error in running index writing transaction", e);
            try {
                indexWriter.rollback();
            } catch (IOException e1) {
                log.error("Cannot rollback index writing transaction", e1);
            }
        }
        
        try {
            indexWriter.close();
        } catch (IOException e) {
            log.error("Cannot commit and close IndexWriter", e);
        }
    }
    
    /**
     * Lucene runnable.
     * 
     * @author bgamard
     */
    public interface LuceneRunnable {
        /**
         * Code to run in a Lucene context.
         * 
         * @param indexWriter
         * @throws Exception 
         */
        public abstract void run(IndexWriter indexWriter) throws Exception;
    }
}
