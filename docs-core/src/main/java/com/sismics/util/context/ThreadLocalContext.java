package com.sismics.util.context;

import com.google.common.collect.Lists;
import com.sismics.docs.core.event.TemporaryFileCleanupAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Context associated to a user request, and stored in a ThreadLocal.
 * 
 * @author jtremeaux
 */
public class ThreadLocalContext {
    /**
     * ThreadLocal to store the context.
     */
    private static final ThreadLocal<ThreadLocalContext> threadLocalContext = new ThreadLocal<>();
    
    /**
     * Entity manager.
     */
    private EntityManager entityManager;

    /**
     * List of async events posted during this request.
     */
    private List<Object> asyncEventList = Lists.newArrayList();

    /**
     * List of temporary files created during this request.
     * They are deleted at the end of each request.
     */
    private List<Path> temporaryFileList = Lists.newArrayList();

    /**
     * Private constructor.
     */
    private ThreadLocalContext() {
        // NOP
    }
    
    /**
     * Returns an instance of this thread context.
     * 
     * @return Thread local context
     */
    public static ThreadLocalContext get() {
        ThreadLocalContext context = threadLocalContext.get();
        if (context == null) {
            context = new ThreadLocalContext();
            threadLocalContext.set(context);
        }
        return context;
    }

    /**
     * Cleans up the instance of this thread context.
     */
    public static void cleanup() {
        threadLocalContext.set(null);
    }
    
    /**
     * Getter of entityManager.
     *
     * @return entityManager
     */
    public EntityManager getEntityManager() {
        if (entityManager != null && entityManager.isOpen()) {
            // This disables the L1 cache
            entityManager.flush();
            entityManager.clear();
        }
        return entityManager;
    }

    /**
     * Setter of entityManager.
     *
     * @param entityManager entityManager
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Add an async event to the queue to be fired after the current request.
     *
     * @param asyncEvent Async event
     */
    public void addAsyncEvent(Object asyncEvent) {
        asyncEventList.add(asyncEvent);
    }

    /**
     * Create a temporary file linked to the request.
     *
     * @return New temporary file
     */
    public Path createTemporaryFile() throws IOException {
        Path path = Files.createTempFile("sismics_docs", null);
        temporaryFileList.add(path);
        return path;
    }

    /**
     * Fire all pending async events.
     */
    public void fireAllAsyncEvents() {
        for (Object asyncEvent : asyncEventList) {
            AppContext.getInstance().getAsyncEventBus().post(asyncEvent);
        }

        if (!temporaryFileList.isEmpty()) {
            // Some files were created during this request, add a cleanup event to the queue
            // It works because we are using a one thread executor
            AppContext.getInstance().getAsyncEventBus().post(new TemporaryFileCleanupAsyncEvent(temporaryFileList));
        }
    }
}
