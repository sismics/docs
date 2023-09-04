package com.sismics.rest.util;

import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Rest utilities.
 *
 * @author bgamard
 */
public class RestUtil {
    /**
     * Transform a File into its JSON representation.
     * If the file size it is not stored in the database the size can be wrong
     * because the encrypted file size is used.
     * @param fileDb a file
     * @return the JSON
     */
    public static JsonObjectBuilder fileToJsonObjectBuilder(File fileDb) {
        try {
            long fileSize = fileDb.getSize().equals(File.UNKNOWN_SIZE) ? Files.size(DirectoryUtil.getStorageDirectory().resolve(fileDb.getId())) : fileDb.getSize();
            return Json.createObjectBuilder()
                    .add("id", fileDb.getId())
                    .add("processing", FileUtil.isProcessingFile(fileDb.getId()))
                    .add("name", JsonUtil.nullable(fileDb.getName()))
                    .add("version", fileDb.getVersion())
                    .add("mimetype", fileDb.getMimeType())
                    .add("document_id", JsonUtil.nullable(fileDb.getDocumentId()))
                    .add("create_date", fileDb.getCreateDate().getTime())
                    .add("size", fileSize);
        } catch (IOException e) {
            throw new ServerException("FileError", "Unable to get the size of " + fileDb.getId(), e);
        }
    }
}
