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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service that retrieve files sizes when they are not in the database.
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

    private static final int BATCH_SIZE = 30;
    
    @Override
    protected void runOneIteration() {
        try {
            TransactionUtil.handle(() -> {
                FileDao fileDao = new FileDao();
                List<File> files = fileDao.getFilesWithUnknownSize(BATCH_SIZE);
                for(File file : files) {
                    processFile(file);
                }
                if(files.size() < BATCH_SIZE) {
                    log.info("No more file to process, stopping the service");
                    stopAsync();
                }
            });
        } catch (Throwable e) {
            log.error("Exception during file service iteration", e);
        }
    }

    void processFile(File file) {
        UserDao userDao = new UserDao();
        User user = userDao.getById(file.getUserId());
        if(user == null) {
            return;
        }

        long fileSize = FileUtil.getFileSize(file.getId(), user);
        if(fileSize != File.UNKNOWN_SIZE){
            FileDao fileDao = new FileDao();
            file.setSize(fileSize);
            fileDao.update(file);
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
    }
}
