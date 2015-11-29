package com.sismics.docs.rest.resource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.util.log4j.LogCriteria;
import com.sismics.util.log4j.LogEntry;
import com.sismics.util.log4j.MemoryAppender;

/**
 * General app REST resource.
 * 
 * @author jtremeaux
 */
@Path("/app")
public class AppResource extends BaseResource {
    /**
     * Return the information about the application.
     * 
     * @return Response
     */
    @GET
    public Response info() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        ResourceBundle configBundle = ConfigUtil.getConfigBundle();
        String currentVersion = configBundle.getString("api.current_version");
        String minVersion = configBundle.getString("api.min_version");

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("current_version", currentVersion.replace("-SNAPSHOT", ""))
                .add("min_version", minVersion)
                .add("total_memory", Runtime.getRuntime().totalMemory())
                .add("free_memory", Runtime.getRuntime().freeMemory());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Retrieve the application logs.
     * 
     * @param level Filter on logging level
     * @param tag Filter on logger name / tag
     * @param message Filter on message
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("log")
    public Response log(
            @QueryParam("level") String level,
            @QueryParam("tag") String tag,
            @QueryParam("message") String message,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        // TODO Change level by minLevel (returns all logs above)

        // Get the memory appender
        Logger logger = Logger.getRootLogger();
        Appender appender = logger.getAppender("MEMORY");
        if (appender == null || !(appender instanceof MemoryAppender)) {
            throw new ServerException("ServerError", "MEMORY appender not configured");
        }
        MemoryAppender memoryAppender = (MemoryAppender) appender;
        
        // Find the logs
        LogCriteria logCriteria = new LogCriteria();
        logCriteria.setLevel(StringUtils.stripToNull(level));
        logCriteria.setTag(StringUtils.stripToNull(tag));
        logCriteria.setMessage(StringUtils.stripToNull(message));
        
        PaginatedList<LogEntry> paginatedList = PaginatedLists.create(limit, offset);
        memoryAppender.find(logCriteria, paginatedList);
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (LogEntry logEntry : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("date", logEntry.getTimestamp())
                    .add("level", logEntry.getLevel())
                    .add("tag", logEntry.getTag())
                    .add("message", logEntry.getMessage()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("total", paginatedList.getResultCount())
                .add("logs", logs);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Destroy and rebuild Lucene index.
     * 
     * @return Response
     */
    @POST
    @Path("batch/reindex")
    public Response batchReindex() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        try {
            AppContext.getInstance().getIndexingService().rebuildIndex();
        } catch (Exception e) {
            throw new ServerException("IndexingError", "Error rebuilding index", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Clean storage.
     * 
     * @return Response
     */
    @POST
    @Path("batch/clean_storage")
    public Response batchCleanStorage() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get all files
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findAll();
        Map<String, File> fileMap = new HashMap<>();
        for (File file : fileList) {
            fileMap.put(file.getId(), file);
        }
        
        // Check if each stored file is valid
        try (DirectoryStream<java.nio.file.Path> storedFileList = Files.newDirectoryStream(DirectoryUtil.getStorageDirectory())) {
            for (java.nio.file.Path storedFile : storedFileList) {
                String fileName = storedFile.getFileName().toString();
                String[] fileNameArray = fileName.split("_");
                if (!fileMap.containsKey(fileNameArray[0])) {
                    Files.delete(storedFile);
                }
            }
        } catch (IOException e) {
            throw new ServerException("FileError", "Error deleting orphan files", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Recompute the quota for each user.
     * 
     * @return Response
     */
    @POST
    @Path("batch/recompute_quota")
    public Response batchRecomputeQuota() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get all files
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findAll();
        
        // Count each file for the corresponding user quota
        UserDao userDao = new UserDao();
        Map<String, User> userMap = new HashMap<>();
        for (File file : fileList) {
            java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
            User user = null;
            if (userMap.containsKey(file.getUserId())) {
                user = userMap.get(file.getUserId());
            } else {
                user = userDao.getById(file.getUserId());
                user.setStorageCurrent(0l);
                userMap.put(user.getId(), user);
            }
            
            try {
                user.setStorageCurrent(user.getStorageCurrent() + Files.size(storedFile));
            } catch (IOException e) {
                throw new ServerException("MissingFile", "File does not exist", e);
            }
        }
        
        // Save all users
        for (User user : userMap.values()) {
            userDao.update(user);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
