package com.sismics.docs.core.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.TransactionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service that retrieve file size when they are not in the database.
 */
public class FileSizeService extends AbstractScheduledService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileSizeService.class);

    public FileSizeService() {
    }

    @Override
    protected void startUp() {
        log.info("File size service starting up");
    }

    @Override
    protected void shutDown() {
        log.info("File size service shutting down");
    }
    
    @Override
    protected void runOneIteration() {
        try {
            TransactionUtil.handle(() -> {
                FileDao fileDao = new FileDao();
                List<File> files = fileDao.getFilesWithoutSize(100);
                if(files.isEmpty()) {
                    shutDown();
                    return;
                }
                for(File file : files) {
                    processFile(file);
                }
            });
        } catch (Throwable e) {
            log.error("Exception during file service iteration", e);
        }
    }

    private void processFile(File file) {
        UserDao userDao = new UserDao();
        User user = userDao.getById(file.getUserId());
        if(user == null) {
            return;
        }

        file.getUserId();
        long fileSize = FileUtil.getFileSize(file.getId(), user);
        if(fileSize != -1){
            FileDao fileDao = new FileDao();
            fileDao.update(file);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
    }
}
