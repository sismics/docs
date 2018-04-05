package com.sismics.util.context;

import com.google.common.collect.Lists;
import com.sismics.docs.core.model.context.AppContext;

import javax.persistence.EntityManager;
import java.util.Iterator;
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
     * Fire all pending async events.
     */
    public void fireAllAsyncEvents() {
        Iterator<Object> iterator = asyncEventList.iterator();
        while (iterator.hasNext()) {
            Object asyncEvent = iterator.next();
            iterator.remove();
            AppContext.getInstance().getAsyncEventBus().post(asyncEvent);
        }
    }
}
