package com.sismics.docs.core.model.context;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.listener.async.*;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.service.FileService;
import com.sismics.docs.core.service.FileSizeService;
import com.sismics.docs.core.service.InboxService;
import com.sismics.docs.core.util.PdfUtil;
import com.sismics.docs.core.util.indexing.IndexingHandler;
import com.sismics.util.ClasspathScanner;
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
     * File service.
     */
    private FileService fileService;

    /**
     * File size service.
     */
    private FileSizeService fileSizeService;

    /**
     * Asynchronous executors.
     */
    private List<ThreadPoolExecutor> asyncExecutorList;

    /**
     * Start the application context.
     */
    private void startUp() {
        resetEventBus();

        // Start indexing handler
        try {
            List<Class<? extends IndexingHandler>> indexingHandlerList = Lists.newArrayList(
                    new ClasspathScanner<IndexingHandler>().findClasses(IndexingHandler.class, "com.sismics.docs.core.util.indexing"));
            for (Class<? extends IndexingHandler> handlerClass : indexingHandlerList) {
                IndexingHandler handler = handlerClass.getDeclaredConstructor().newInstance();
                if (handler.accept()) {
                    indexingHandler = handler;
                    break;
                }
            }
            indexingHandler.startUp();
        } catch (Exception e) {
            log.error("Error starting the indexing handler", e);
        }

        // Start file service
        fileService = new FileService();
        fileService.startAsync();
        fileService.awaitRunning();

        // Start inbox service
        inboxService = new InboxService();
        inboxService.startAsync();
        inboxService.awaitRunning();

        // Start file size service
        fileSizeService = new FileSizeService();
        fileSizeService.startAsync();
        fileSizeService.awaitRunning();

        // Register fonts
        PdfUtil.registerFonts();

        // Change the admin password if needed
        String envAdminPassword = System.getenv(Constants.ADMIN_PASSWORD_INIT_ENV);
        if (!Strings.isNullOrEmpty(envAdminPassword)) {
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword())) {
                adminUser.setPassword(envAdminPassword);
                userDao.updateHashedPassword(adminUser);
            }
        }

        // Change the admin email if needed
        String envAdminEmail = System.getenv(Constants.ADMIN_EMAIL_INIT_ENV);
        if (!Strings.isNullOrEmpty(envAdminEmail)) {
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
        asyncEventBus.register(new AclCreatedAsyncListener());
        asyncEventBus.register(new AclDeletedAsyncListener());
        asyncEventBus.register(new WebhookAsyncListener());

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
            int threadCount = Math.max(Runtime.getRuntime().availableProcessors() / 2, 2);
            ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount,
                    1L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<>());
            asyncExecutorList.add(executor);
            return new AsyncEventBus(executor);
        }
    }

    /**
     * Return the current number of queued tasks waiting to be processed.
     *
     * @return Number of queued tasks
     */
    public int getQueuedTaskCount() {
        int queueSize = 0;
        for (ThreadPoolExecutor executor : asyncExecutorList) {
            queueSize += executor.getTaskCount() - executor.getCompletedTaskCount();
        }
        return queueSize;
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

    public FileService getFileService() {
        return fileService;
    }

    public void shutDown() {
        for (ExecutorService executor : asyncExecutorList) {
            // Shutdown executor, don't accept any more tasks (can cause error with nested events)
            try {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.MINUTES);
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

        if (fileService != null) {
            fileService.stopAsync();
        }

        if (fileSizeService != null) {
            fileSizeService.stopAsync();
        }

        instance = null;
    }
}
