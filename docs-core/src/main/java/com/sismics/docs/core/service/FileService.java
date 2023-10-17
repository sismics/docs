package com.sismics.docs.core.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * File service.
 *
 * @author bgamard
 */
public class FileService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    /**
     * Phantom references queue.
     */
    private final ReferenceQueue<Path> referenceQueue = new ReferenceQueue<>();
    private final Set<TemporaryPathReference> referenceSet = new HashSet<>();

    public FileService() {
    }

    @Override
    protected void startUp() {
        log.info("File service starting up");
    }

    @Override
    protected void shutDown() {
        log.info("File service shutting down");
    }
    
    @Override
    protected void runOneIteration() {
        try {
            deleteTemporaryFiles();
        } catch (Throwable e) {
            log.error("Exception during file service iteration", e);
        }
    }

    /**
     * Delete unreferenced temporary files.
     */
    private void deleteTemporaryFiles() throws Exception {
        TemporaryPathReference ref;
        while ((ref = (TemporaryPathReference) referenceQueue.poll()) != null) {
            Files.delete(Paths.get(ref.path));
            referenceSet.remove(ref);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
    }

    public Path createTemporaryFile() throws IOException {
        return createTemporaryFile(null);
    }

    /**
     * Create a temporary file.
     *
     * @param name Wanted file name
     * @return New temporary file
     */
    public Path createTemporaryFile(String name) throws IOException {
        Path path = Files.createTempFile("sismics_docs", name);
        referenceSet.add(new TemporaryPathReference(path, referenceQueue));
        return path;
    }

    /**
     * Phantom reference to a temporary file.
     *
     * @author bgamard
     */
    static class TemporaryPathReference extends PhantomReference<Path> {
        String path;
        TemporaryPathReference(Path referent, ReferenceQueue<? super Path> q) {
            super(referent, q);
            path = referent.toAbsolutePath().toString();
        }
    }
}
