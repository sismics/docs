package com.sismics.docs.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.docs.core.event.TemporaryFileCleanupAsyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Listener to cleanup temporary files created during a request.
 *
 * @author bgamard
 */
public class TemporaryFileCleanupAsyncListener {
	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(TemporaryFileCleanupAsyncListener.class);

	/**
	 * Cleanup temporary files.
	 *
	 * @param event Temporary file cleanup event
	 * @throws Exception e
	 */
	@Subscribe
	public void on(final TemporaryFileCleanupAsyncEvent event) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Cleanup temporary files event: " + event.toString());
		}

		for (Path file : event.getFileList()) {
			Files.delete(file);
		}
	}
}
