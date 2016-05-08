package com.sismics.docs.core.model.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.dao.jpa.ConfigDao;
import com.sismics.docs.core.listener.async.DocumentCreatedAsyncListener;
import com.sismics.docs.core.listener.async.DocumentDeletedAsyncListener;
import com.sismics.docs.core.listener.async.DocumentUpdatedAsyncListener;
import com.sismics.docs.core.listener.async.FileCreatedAsyncListener;
import com.sismics.docs.core.listener.async.FileDeletedAsyncListener;
import com.sismics.docs.core.listener.async.RebuildIndexAsyncListener;
import com.sismics.docs.core.listener.sync.DeadEventListener;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.service.IndexingService;
import com.sismics.util.EnvironmentUtil;

/**
 * Global application context.
 *
 * @author jtremeaux 
 */
public class AppContext {
    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Event bus.
     */
    private EventBus eventBus;
    
    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;
    
    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();
        
        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAsync();
    }
    
    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        eventBus = new EventBus();
        eventBus.register(new DeadEventListener());
        
        asyncExecutorList = new ArrayList<>();
        
        asyncEventBus = newAsyncEventBus();
        asyncEventBus.register(new FileCreatedAsyncListener());
        asyncEventBus.register(new FileDeletedAsyncListener());
        asyncEventBus.register(new DocumentCreatedAsyncListener());
        asyncEventBus.register(new DocumentUpdatedAsyncListener());
        asyncEventBus.register(new DocumentDeletedAsyncListener());
        asyncEventBus.register(new RebuildIndexAsyncListener());
    }

    /**
     * Returns a single instance of the application context.
     * 
     * @return Application context
     */
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    /**
     * Wait for termination of all asynchronous events.
     * /!\ Must be used only in unit tests and never a multi-user environment. 
     */
    public void waitForAsync() {
        if (EnvironmentUtil.isUnitTest()) {
            return;
        }
        try {
            for (ExecutorService executor : asyncExecutorList) {
                // Shutdown executor, don't accept any more tasks (can cause error with nested events)
                try {
                    executor.shutdown();
                    executor.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // NOP
                }
            }
        } finally {
            resetEventBus();
        }
    }

    /**
     * Creates a new asynchronous event bus.
     * 
     * @return Async event bus
     */
    private EventBus newAsyncEventBus() {
        if (EnvironmentUtil.isUnitTest()) {
            return new EventBus();
        } else {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Getter of eventBus.
     *
     * @return eventBus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Getter of asyncEventBus.
     *
     * @return asyncEventBus
     */
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    /**
     * Getter of indexingService.
     *
     * @return indexingService
     */
    public IndexingService getIndexingService() {
        return indexingService;
    }
}
