package com.sismics.docs.core.model.context;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.event.RebuildIndexAsyncEvent;
import com.sismics.docs.core.listener.async.*;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.service.InboxService;
import com.sismics.docs.core.util.PdfUtil;
import com.sismics.docs.core.util.indexing.IndexingHandler;
import com.sismics.docs.core.util.indexing.LuceneIndexingHandler;
import com.sismics.util.EnvironmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    /**
     * Singleton instance.
     */
    private static AppContext instance;

    /**
     * Generic asynchronous event bus.
     */
    private EventBus asyncEventBus;

    /**
     * Asynchronous bus for email sending.
     */
    private EventBus mailEventBus;

    /**
     * Indexing handler.
     */
    private IndexingHandler indexingHandler;

    /**
     * Inbox scanning service.
     */
    private InboxService inboxService;

    /**
     * Asynchronous executors.
     */
    private List<ExecutorService> asyncExecutorList;

    /**
     * Start the application context.
     */
    private void startUp() {
        resetEventBus();

        // Start indexing handler
        indexingHandler = new LuceneIndexingHandler();
        try {
            indexingHandler.startUp();
        } catch (Exception e) {
            log.error("Error starting the indexing handler, rebuilding the index: " + e.getMessage());
            RebuildIndexAsyncEvent rebuildIndexAsyncEvent = new RebuildIndexAsyncEvent();
            asyncEventBus.post(rebuildIndexAsyncEvent);
        }

        // Start inbox service
        inboxService = new InboxService();
        inboxService.startAsync();
        inboxService.awaitRunning();

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

        // Change the admin email if needed
        String envAdminEmail = System.getenv(Constants.ADMIN_EMAIL_INIT_ENV);
        if (envAdminEmail != null) {
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (Constants.DEFAULT_ADMIN_EMAIL.equals(adminUser.getEmail())) {
                adminUser.setEmail(envAdminEmail);
                userDao.update(adminUser, "admin");
            }
        }
    }

    /**
     * (Re)-initializes the event buses.
     */
    private void resetEventBus() {
        asyncExecutorList = new ArrayList<>();

        asyncEventBus = newAsyncEventBus();
        asyncEventBus.register(new FileProcessingAsyncListener());
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
            instance.startUp();
        }
        return instance;
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
                    new LinkedBlockingQueue<>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

    public EventBus getMailEventBus() {
        return mailEventBus;
    }

    public IndexingHandler getIndexingHandler() {
        return indexingHandler;
    }

    public InboxService getInboxService() {
        return inboxService;
    }

    public void shutDown() {
        for (ExecutorService executor : asyncExecutorList) {
            // Shutdown executor, don't accept any more tasks (can cause error with nested events)
            try {
                executor.shutdown();
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // NOP
            }
        }

        if (indexingHandler != null) {
            indexingHandler.shutDown();
        }

        if (inboxService != null) {
            inboxService.stopAsync();
            inboxService.awaitTerminated();
        }

        instance = null;
    }
}
