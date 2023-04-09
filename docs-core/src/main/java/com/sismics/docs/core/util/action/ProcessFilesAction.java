package com.sismics.docs.core.util.action;

import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.event.FileUpdatedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.util.context.ThreadLocalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;
import java.nio.file.Path;
import java.util.List;

/**
 * Action to process all files.
 *
 * @author bgamard
 */
public class ProcessFilesAction implements Action {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ProcessFilesAction.class);

    @Override
    public void execute(DocumentDto documentDto, JsonObject action) {
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.getByDocumentId(null, documentDto.getId());

        try {
            for (File file : fileList) {
                // Get the creating user
                UserDao userDao = new UserDao();
                User user = userDao.getById(file.getUserId());

                // Decrypt the file
                Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
                Path unencryptedFile = EncryptionUtil.decryptFile(storedFile, user.getPrivateKey());

                // Start the asynchronous processing
                FileUtil.startProcessingFile(file.getId());
                FileUpdatedAsyncEvent event = new FileUpdatedAsyncEvent();
                event.setUserId("admin");
                event.setLanguage(documentDto.getLanguage());
                event.setFileId(file.getId());
                event.setUnencryptedFile(unencryptedFile);
                ThreadLocalContext.get().addAsyncEvent(event);
            }
        } catch (Exception e) {
            log.error("Error processing a file", e);
        }
    }

    @Override
    public void validate(JsonObject action) {
        // No parameter, so always OK
    }
}
