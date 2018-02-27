package com.sismics.docs.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.ConfigDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.listener.async.*;
import com.sismics.docs.core.listener.sync.DeadEventListener;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.service.InboxService;
import com.sismics.docs.core.service.IndexingService;
import com.sismics.docs.core.util.PdfUtil;
import com.sismics.util.EnvironmentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
     * Asynchronous bus for email sending.
     */
    private EventBus mailEventBus;

    /**
     * Indexing service.
     */
    private IndexingService indexingService;

    /**
     * Inbox scanning service.
     */
    private InboxService inboxService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;
    
    /**
     * Private constructor.
     */
    private AppContext() {
        resetEventBus();

        // Start indexing service
        ConfigDao configDao = new ConfigDao();
        Config luceneStorageConfig = configDao.getById(ConfigType.LUCENE_DIRECTORY_STORAGE);
        indexingService = new IndexingService(luceneStorageConfig != null ? luceneStorageConfig.getValue() : null);
        indexingService.startAsync();

        // Start inbox service
        inboxService = new InboxService();
        inboxService.startAsync();

        // Register fonts
        PdfUtil.registerFonts();

        // Change the admin password if needed
        String envAdminPassword = System.getenv(Constants.ADMIN_PASSWORD_INIT_ENV);
        if (envAdminPassword != null) {
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword())) {
                adminUser.setPassword(envAdminPassword);
                userDao.updateHashedPassword(adminUser);
            }
        }
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
        asyncEventBus.register(new TemporaryFileCleanupAsyncListener());

        mailEventBus = newAsyncEventBus();
        mailEventBus.register(new PasswordLostAsyncListener());
        mailEventBus.register(new RouteStepValidateAsyncListener());
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
            // /!\ Don't add more threads because a cleanup event is fired at the end of each request
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    public EventBus getMailEventBus() {
        return mailEventBus;
    }

    public IndexingService getIndexingService() {
        return indexingService;
    }

    public InboxService getInboxService() {
        return inboxService;
    }
}
