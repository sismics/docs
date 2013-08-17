package com.sismics.docs.core.listener.async;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.event.OcrFileAsyncEvent;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;

/**
 * Listener on OCR all files in database.
 * 
 * @author bgamard
 */
public class OcrFileAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(OcrFileAsyncListener.class);

    /**
     * OCR all files.
     * 
     * @param ocrFileAsyncEvent OCR all files in database event
     * @throws Exception
     */
    @Subscribe
    public void on(final OcrFileAsyncEvent ocrFileAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("OCR all files in database event: " + ocrFileAsyncEvent.toString());
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
                    String content = FileUtil.ocrFile(document, file);
                    file.setContent(content);
                    TransactionUtil.commit();
                    log.info(MessageFormat.format("File OCR-ized in {0}ms", System.currentTimeMillis() - startTime));
                }
            }
        });
    }
}
