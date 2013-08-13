package com.sismics.docs.core.util;

import com.sismics.docs.core.dao.lucene.DocsStandardAnalyzer;
import com.sismics.docs.core.model.context.AppContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, new DocsStandardAnalyzer(Version.LUCENE_42));
        
        // Merge sequentially, because Lucene writing is already done asynchronously 
        config.setMergeScheduler(new SerialMergeScheduler());
        
        // Creating index writer
        Directory directory = AppContext.getInstance().getLuceneDirectory();
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, config);
        } catch (IOException e) {
            log.error("Cannot create IndexWriter", e);
        }

        // Unlock index if needed
        try {
            if (IndexWriter.isLocked(directory)) {
                IndexWriter.unlock(directory);
            }
        } catch (IOException e) {
            log.error("Cannot unlock Lucene directory", e);
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
            log.error("Cannot close IndexWriter", e);
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
