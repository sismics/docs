package com.sismics.docs.core.listener.async;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.util.ImageUtil;

/**
 * Listener on new file.
 * 
 * @author bgamard
 */
public class FileCreatedAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileCreatedAsyncListener.class);

    /**
     * Process new file.
     * 
     * @param fileCreatedAsyncEvent New file created event
     * @throws Exception
     */
    @Subscribe
    public void onFileCreated(final FileCreatedAsyncEvent fileCreatedAsyncEvent) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("File created event: " + fileCreatedAsyncEvent.toString());
        }

        // OCR the file if it is an image
        if (ImageUtil.isImage(fileCreatedAsyncEvent.getFile().getMimeType())) {
            long startTime = System.currentTimeMillis();
            FileUtil.ocrFile(fileCreatedAsyncEvent.getDocument(), fileCreatedAsyncEvent.getFile());
            log.info(MessageFormat.format("File OCR-ized in {0}ms", System.currentTimeMillis() - startTime));
        }
    }
}
